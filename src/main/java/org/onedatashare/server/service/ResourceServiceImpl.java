package org.onedatashare.server.service;

import org.onedatashare.server.model.credential.UploadCredential;
import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.GlobusWebClientCredential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.clientupload.ClientUploadSession;
import org.onedatashare.server.module.dropbox.DbxSession;
import org.onedatashare.server.module.googledrive.GoogleDriveResource;
import org.onedatashare.server.module.googledrive.GoogleDriveSession;
import org.onedatashare.server.module.gridftp.GridftpSession;
import org.onedatashare.server.module.vfs.VfsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ResourceServiceImpl implements ResourceService<Resource>  {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    private HashMap<UUID, Disposable> ongoingJobs = new HashMap<>();

    public Mono<? extends Resource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.uri);
        String id = userAction.id;
        ArrayList<IdMap> idMap = userAction.map;
        return userService.getLoggedInUser(cookie)
                .map(User::getCredentials)
                .map(uuidCredentialMap -> uuidCredentialMap.get(UUID.fromString(userAction.credential.getUuid())))
                .map(credential -> new GoogleDriveSession(URI.create(userAction.uri), credential))
                .flatMap(GoogleDriveSession::initialize)
                .flatMap(driveSession -> driveSession.select(path,id, idMap))
                .onErrorResume(throwable -> throwable instanceof TokenExpiredException, throwable ->
                    Mono.just(userService.updateCredential(cookie,((TokenExpiredException)throwable).cred))
                            .map(credential -> new GoogleDriveSession(URI.create(userAction.uri), credential))
                            .flatMap(GoogleDriveSession::initialize)
                            .flatMap(driveSession -> driveSession.select(path,id, idMap))
                );
    }

    public Mono<Resource> getResourceWithUserActionResource(String cookie, UserActionResource userActionResource) {
        final String path = pathFromUri(userActionResource.uri);
        String id = userActionResource.id;
        ArrayList<IdMap> idMap = userActionResource.map;

        return userService.getLoggedInUser(cookie)
                .map(user -> createCredential(userActionResource, user))
                .map(credential -> createSession(userActionResource.uri, credential))
                .flatMap(session -> session.initialize())
                .flatMap(session -> ((Session)session).select(path,id,idMap));
    }

    public String pathFromUri(String uri) {
        String path = "";
        if(uri.contains("dropbox://")){
            path = uri.split("dropbox://")[1];
        }else if(uri.contains("googledrive:/")){
            path = uri.split("googledrive:")[1];
        }else path = uri;

        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public Credential createCredential(UserActionResource userActionResource, User user) {
        if(userActionResource.uri.contains("dropbox://") || userActionResource.uri.contains("googledrive:/")){
            return user.getCredentials().get(UUID.fromString(userActionResource.credential.getUuid()));
        }else if(userActionResource.uri.equals("Upload")){
            return userActionResource.uploader;
        }else if(userActionResource.uri.startsWith("gsiftp://")){

            GlobusClient gc = userService.getGlobusClientFromUser(user);
            return new GlobusWebClientCredential(userActionResource.credential.getGlobusEndpoint(), gc);
        }
        else return new UserInfoCredential(userActionResource.credential);
    }


    public Session createSession(String uri, Credential credential) {
        if(uri.contains("dropbox://")){
            return new DbxSession(URI.create(uri), credential);
        }else if(uri.contains("Upload")){
            UploadCredential upc = (UploadCredential)credential;
            return new ClientUploadSession(upc.getFux(), upc.getSize(), upc.getName());
        }else if(uri.contains("googledrive:/")){
            return new GoogleDriveSession(URI.create(uri), credential);
        }else if(credential instanceof GlobusWebClientCredential){
            return new GridftpSession(URI.create(uri), credential);
        }
        else return new VfsSession(URI.create(uri), credential);
    }

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(Resource::stat);
    }

    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::mkdir)
                .flatMap(resource -> ((Resource)resource).stat());
    }

    public Mono<Resource> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::delete);
    }

    public Mono<Job> submit(String cookie, UserAction userAction) {
        return userService.getLoggedInUser(cookie)
            .map(user -> {
                Job job = new Job(userAction.src, userAction.dest);
                job.setStatus(JobStatus.scheduled);
                job = user.saveJob(job);
                userService.saveUser(user).subscribe();
                return job;
            })
            .flatMap(jobService::saveJob)
            .doOnSuccess(job -> processTransferFromJob(job, cookie))
            .subscribeOn(Schedulers.elastic());
    }

    //@Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::download);
    }

    public Mono<Job> restartJob(String cookie, UserAction userAction){
        return userService.getLoggedInUser(cookie)
            .flatMap(user ->{
                return jobService.findJobByJobId(cookie, userAction.job_id)
                    .flatMap(job -> {
                        Job restartedJob = new Job(job.getSrc(), job.getDest());
                        boolean credsExists = updateJobCredentials(user, job);
                        if(!credsExists){
                            return Mono.error(new Exception("Restart job failed since either or both credentials of the job do not exist"));
                        }
                        restartedJob.setStatus(JobStatus.scheduled);
                        restartedJob.setRestartedJob(true);
                        restartedJob.setSourceJob(userAction.job_id);
                        restartedJob = user.saveJob(restartedJob);
                        userService.saveUser(user).subscribe();
                        return Mono.just(restartedJob);
                    })
                    .flatMap(jobService::saveJob)
                    .doOnSuccess(restartedJob -> processTransferFromJob(restartedJob, cookie));
            })
            .subscribeOn(Schedulers.elastic());
    }

    public Mono<Job> deleteJob(String cookie, UserAction userAction){
        return jobService.findJobByJobId(cookie,userAction.job_id)
                .map(job -> {
                    job.setDeleted(true);
                    return job;
                }).flatMap(jobService::saveJob).subscribeOn(Schedulers.elastic());
    }

    public Mono<Job> cancel(String cookie, UserAction userAction) {
        return userService.getLoggedInUser(cookie)
                .flatMap(user -> {
                    return jobService.findJobByJobId(cookie, userAction.job_id)
                            .map(job -> {
                                ongoingJobs.get(job.getUuid()).dispose();
                                ongoingJobs.remove(job.getUuid());
                                return job.setStatus(JobStatus.removed);
                            });
                })
                .subscribeOn(Schedulers.elastic());
    }

    public boolean updateJobCredentials(User user, Job restartedJob){
        boolean credsExist = true;
        if(restartedJob.getSrc().getCredential() != null) {
            UUID srcCredUUID = getCredUuidUsingCredName(user, restartedJob.getSrc().getCredential().getName());
            if(srcCredUUID != null){
                if(!UUID.fromString(restartedJob.getSrc().getCredential().getUuid()).equals(srcCredUUID)){
                    restartedJob.getSrc().getCredential().setUuid(srcCredUUID.toString());
                }
            }
            else
                credsExist = false;

        }

        if(!credsExist)
            return credsExist;    // don't want to check for dest cred if src cred doesn't exist

        if(restartedJob.getDest().getCredential() != null) {
            UUID destCredUUID = getCredUuidUsingCredName(user, restartedJob.getDest().getCredential().getName());
            if(destCredUUID != null){
                if(!UUID.fromString(restartedJob.getDest().getCredential().getUuid()).equals(destCredUUID)){
                    restartedJob.getDest().getCredential().setUuid(destCredUUID.toString());
                }
            }
            else
                credsExist = false;
        }

        return credsExist;
    }

    public UUID getCredUuidUsingCredName(User user, String credName){
        for(Map.Entry<UUID, Credential> userCredEntry : user.getCredentials().entrySet()){
            if(userCredEntry.getValue() instanceof OAuthCredential){
                OAuthCredential cred = (OAuthCredential) userCredEntry.getValue();
                if(cred.getName().equals(credName)){
                    return  userCredEntry.getKey();
                }
            }
        }
        return null;
    }

    public void processTransferFromJob(Job job, String cookie) {
        Transfer<Resource, Resource> transfer = new Transfer<>();
        Disposable ongoingJob = getResourceWithUserActionResource(cookie, job.getSrc())
            .map(transfer::setSource)
            .flatMap(t -> getResourceWithUserActionResource(cookie, job.getDest()))
            .map(transfer::setDestination)
            .flux()
            .flatMap(transfer1 -> transfer1.start(1L << 20))
            .doOnSubscribe(s -> job.setStatus(JobStatus.processing))
            .doOnCancel(new RunnableCanceler(job))
            .doFinally(s -> {
                if (job.getStatus() != JobStatus.removed)
                    job.setStatus(JobStatus.complete);
                jobService.saveJob(job).subscribe();
                ongoingJobs.remove(job.getUuid());
            })
            .map(job::updateJobWithTransferInfo)
            .flatMap(jobService::saveJob)
            .subscribe();
        ongoingJobs.put(job.getUuid(), ongoingJob);
    }

    class RunnableCanceler implements Runnable {
        Job job;

        public RunnableCanceler(Job job) {
            this.job = job;
        }

        @Override
        public void run() {
            job.setStatus(JobStatus.removed);
//            ongoingJobs.remove(job.uuid);
        }
    }
}
