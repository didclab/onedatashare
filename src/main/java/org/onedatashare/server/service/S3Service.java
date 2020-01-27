package org.onedatashare.server.service;

import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.s3.S3Resource;
import org.onedatashare.server.module.s3.S3Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

import static org.onedatashare.server.model.core.ODSConstants.TRANSFER_SLICE_SIZE;

@Service
public class S3Service implements ResourceService<S3Resource> {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;


    public Mono<S3Resource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        return userService.getLoggedInUser(cookie)
                .map(User::getCredentials)
                .map(creds -> {
                    Credential credential = creds.get(userAction.getUuid());
                    if (credential != null)
                        return credential;
                    else if (userAction.getCredential().getUsername() == null || userAction.getCredential().getPassword() == null) {
                        Mono.error(new Exception("Invalid"));
                    }
                    return new UserInfoCredential(userAction.getCredential());
                })
                .map(credential -> new S3Session(URI.create(userAction.getUri()), (Credential) credential))
                .flatMap(S3Session::initialize)
                .flatMap(s3Session -> {
                    String path = userAction.getUri();
                    return s3Session.select(path);
                });

//        return userService.getLoggedInUser(cookie)
//                .map(User::getCredentials)
//                .map(uuidCredentialMap -> uuidCredentialMap.get(UUID.fromString(userAction.getCredential().getUuid())))
//                .map(credential -> new S3Session(URI.create(userAction.getUri()), credential))
//                .flatMap(S3Session::initialize)
//                .flatMap(s3Session -> s3Session.select(path, id, idMap));
    }

    public Mono<S3Resource> getResourceWithUserActionResource(String cookie, UserActionResource userActionResource) {
        return null;
    }


    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(S3Resource::stat);
    }

    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(S3Resource::mkdir)
                .flatMap(S3Resource::stat);
    }

    public Mono<S3Resource> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(S3Resource::delete);
    }

    @Override
    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }

    public Mono<String> getDownloadURL(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(S3Resource::getDownloadURL);
    }

}
