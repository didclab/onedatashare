package org.onedatashare.server.service;

import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.UUID;

@Service
public class DbxService implements ResourceService<DbxResource>{

  @Autowired
  private UserService userService;

  @Autowired
  private JobService jobService;

  public Mono<DbxResource> getDbxResourceWithUserActionUri(String cookie, UserAction userAction) {
    final String path = pathFromDbxUri(userAction.uri);
    return userService.getLoggedInUser(cookie)
            .map(User::getCredentials)
            .map(uuidCredentialMap -> uuidCredentialMap.get(UUID.fromString(userAction.credential.getUuid())))
            .map(credential -> new DbxSession(URI.create(userAction.uri), credential))
            .flatMap(DbxSession::initialize)
            .flatMap(dbxSession -> dbxSession.select(path));
  }

  public Mono<DbxResource> getDbxResourceWithJobSourceOrDestination(String cookie, UserActionResource userActionResource) {
    final String path = pathFromDbxUri(userActionResource.uri);
    return userService.getLoggedInUser(cookie)
            .map(User::getCredentials)
            .map(uuidCredentialMap ->
                    uuidCredentialMap.get(UUID.fromString(userActionResource.credential.getUuid())))
            .map(credential -> new DbxSession(URI.create(userActionResource.uri), credential))
            .flatMap(DbxSession::initialize)
            .flatMap(dbxSession -> dbxSession.select(path));
  }

  public String pathFromDbxUri(String uri) {
    String path = "";
    if(uri.contains("dropbox://")){
      path = uri.split("dropbox://")[1];
    }
    try {
      path = java.net.URLDecoder.decode(path, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return path;
  }

  public Mono<Stat> list(String cookie, UserAction userAction) {
    return getDbxResourceWithUserActionUri(cookie, userAction).flatMap(DbxResource::stat);
  }

  public Mono<Stat> mkdir(String cookie, UserAction userAction) {
    return getDbxResourceWithUserActionUri(cookie, userAction)
            .flatMap(DbxResource::mkdir)
            .flatMap(DbxResource::stat);
  }

  public Mono<DbxResource> delete(String cookie, UserAction userAction) {
    return getDbxResourceWithUserActionUri(cookie, userAction)
            .flatMap(DbxResource::delete);
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
    Transfer<DbxResource, DbxResource> transfer = new Transfer<>();
    getDbxResourceWithJobSourceOrDestination(cookie, job.src)
            .map(transfer::setSource)
            .flatMap(t -> getDbxResourceWithJobSourceOrDestination(cookie, job.dest))
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
    return getDbxResourceWithUserActionUri(cookie,userAction)
            .flatMap(DbxResource::generateDownloadLink);
  }

}
