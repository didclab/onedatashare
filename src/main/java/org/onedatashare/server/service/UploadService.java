package org.onedatashare.server.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.model.util.TransferInfo;
import org.onedatashare.server.module.clientupload.ClientUploadResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

@Service
public class UploadService {

    @Autowired
    UserService userService;

    @Autowired
    JobService jobService;

    @Autowired
    ResourceServiceImpl resourceService;

    private static Map<UUID, UploadRecord> ongoingUploads = new HashMap<UUID, UploadRecord>();

    public Mono<Job> uploadChunk(String cookie, UUID uuid,
                            Mono<FilePart> filePart, String credential,
                            String directoryPath, String fileName, Long totalFileSize){
        return userService.getLoggedInUser(cookie)
                .map(user -> {
                    UploadRecord uploadRecord = ongoingUploads.get(uuid);
                    if(uploadRecord == null) {
                        UserActionResource src = new UserActionResource();
                        src.uri = "Local file upload";
                        UserActionCredential userActionCredential = new UserActionCredential();
                        userActionCredential.uuid = credential;
                        UserActionResource dest = new UserActionResource();
                        dest.uri = directoryPath + (directoryPath.endsWith("/")? "":"/") + fileName;
                        dest.credential = userActionCredential;

                        Job job = new Job(src, dest);
                        job.setStatus(JobStatus.scheduled);
                        job = user.saveJob(job);
                        userService.saveUser(user).subscribe();

                        uploadRecord = new UploadRecord();
                        uploadRecord.setUploadJob(job);
                        ongoingUploads.put(uuid, uploadRecord);
                        return job;
                    }
                    else {
                        return uploadRecord.getUploadJob();
                    }
                })
                .flatMap(jobService::saveJob)
                .doOnSuccess(job -> processUpload(cookie, uuid, totalFileSize, filePart))
                .subscribeOn(Schedulers.elastic());

    }

    public Flux<Object> processUpload(String cookie, UUID uuid, Long totalFileSize, Mono<FilePart> filePart){
        UploadRecord uploadRecord = ongoingUploads.get(uuid);
        if(uploadRecord.getUploadTransfer() == null){
            Job job = uploadRecord.getUploadJob();
            UploadTransfer<Resource, Resource> upload = new UploadTransfer<>(totalFileSize);
            uploadRecord.setUploadTransfer(upload);
            return resourceService.getResourceWithUserActionResource(cookie, job.dest)
//                    .map(upload::setDestination)
                    .map(res -> {
                        System.out.println(res);
                        return upload.setDestination(res);
                    })
                    .flux()
                    .map(x -> x.start(filePart))
                    .map(s -> job.setStatus(JobStatus.processing));
//                .doFinally(s -> {
//                    if (job.getStatus() != JobStatus.removed) job.setStatus(JobStatus.complete);
//                    jobService.saveJob(job).subscribe();
//                });
        }
        else{
            return uploadRecord.getUploadTransfer().start(filePart);
        }
    }

    @NoArgsConstructor
    @Data
    class UploadTransfer<S extends Resource, D extends Resource> extends Transfer<S, D>{

        Drain drain;

//        public UploadTransfer(S source, D destination){
//            super(source,destination);
//        }
        public UploadTransfer(Long totalFileSize){
            initialize(totalFileSize);
        }

        public UploadTransfer<S, D> setDestination(D destination) {
            this.destination = destination;
            drain = destination.sink();
            return this;
        }

        public Flux<Object> start(Mono<FilePart> filePart){
            ClientUploadResource uploadRes = new ClientUploadResource();
            ClientUploadResource.ClientUploadTap tap = uploadRes.tap();

            return tap.tap(filePart)
                    .subscribeOn(Schedulers.elastic())
                    .doOnNext(drain::drain)
                    .subscribeOn(Schedulers.elastic())
//                    .doOnSubscribe(s -> startTimer())
                    .map(this::addProgress);
//                    .doOnComplete(drain::finish)
//                    .doFinally(s -> done());
        }

        @Override
        public TransferInfo addProgress(Slice slice) {
            long size = slice.length();
            this.progress.add(size);
            throughput.update(size);
            info.update(timer, progress, throughput);
            return info;
        }

        public void initialize(Long totalFileSize) {
            info.setTotal(totalFileSize);
            this.startTimer();
        }
    }

    @NoArgsConstructor
    @Data
    public class UploadRecord{
        private Job uploadJob;
        private UploadTransfer uploadTransfer;
    }

    public Mono<Integer> finishUpload(UUID uuid){
        UploadRecord uploadRecord = ongoingUploads.get(uuid);
        if(uploadRecord != null){
            Job uploadJob = uploadRecord.getUploadJob();
            if(uploadJob.getStatus() != JobStatus.removed)
                uploadJob.setStatus(JobStatus.complete);
            uploadJob.updateJobWithTransferInfo(uploadRecord.getUploadTransfer().getInfo());
            jobService.saveJob(uploadJob);
            uploadRecord.getUploadTransfer().getDrain().finish();
            uploadRecord.getUploadTransfer().done();
            ongoingUploads.remove(uuid);
            return Mono.just(uploadJob.job_id);
        }
        return Mono.just(-1);
    }
}
