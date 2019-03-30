package org.onedatashare.server.service;

import org.apache.commons.vfs2.FileObject;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.vfs.VfsResource;
import org.onedatashare.server.module.vfs.VfsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

@Service
public class VfsService implements ResourceService<VfsResource> {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    public Mono<VfsResource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.uri);
        return userService.getLoggedInUser(cookie)
                .map(user -> new UserInfoCredential(userAction.credential))
                .map(credential -> new VfsSession(URI.create(userAction.uri), credential))
                .flatMap(VfsSession::initialize)
                .flatMap(vfsSession -> vfsSession.select(path));
    }

    public Mono<VfsResource> getResourceWithUserActionResource(String cookie, UserActionResource userActionResource) {
        final String path = pathFromUri(userActionResource.uri);
        return userService.getLoggedInUser(cookie)
                .map(user -> new UserInfoCredential(userActionResource.credential))
                .map(credential -> new VfsSession(URI.create(userActionResource.uri), credential))
                .flatMap(VfsSession::initialize)
                .flatMap(vfsSession -> vfsSession.select(path));
    }

    public String pathFromUri(String uri) {
        String path = "";
        if(uri.contains("dropbox://")){
            path = uri.split("dropbox://")[1];
        }
        else path = uri;
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(VfsResource::stat);
    }

    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(VfsResource::mkdir)
                .flatMap(VfsResource::stat);
    }

    public Mono<VfsResource> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(VfsResource::delete);
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

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }

    public void processTransferFromJob(Job job, String cookie) {
        Transfer<Resource, Resource> transfer = new Transfer<>();
        getResourceWithUserActionResource(cookie, job.src)
                .map(transfer::setSource)
                .flatMap(t -> getResourceWithUserActionResource(cookie, job.dest))
                .map(transfer::setDestination)
                .flux()
                .flatMap(transfer1 -> transfer1.start(1L << 20))
                .doOnSubscribe(s -> job.setStatus(JobStatus.processing))
                .doFinally(s -> {
                    job.setStatus(JobStatus.complete);
                    jobService.saveJob(job).subscribe();
                })
                .map(job::updateJobWithTransferInfo)
                .flatMap(jobService::saveJob)
                .subscribe();
    }

    public Mono<String> getDownloadURL(String cookie, UserAction userAction){
        return getResourceWithUserActionUri(cookie,userAction).flatMap(VfsResource::getDownloadURL);
    }

    public Mono<VfsResource> getDownloadResponse(String cookie, UserAction userAction){
//        getResourceWithUserActionUri(cookie,)
        return Mono.just(null);
//    return getResourceWithUserActionUri(cookie, userAction).flatMap(VfsResource::getDownloadStream);
    }

    public Mono<FileObject> getDownloadStream(String cookie, UserAction userAction){
        return getResourceWithUserActionUri(cookie,userAction).map(VfsResource::getAcquisition);
    }
}
