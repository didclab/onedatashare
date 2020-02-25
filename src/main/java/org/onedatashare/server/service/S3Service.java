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

    @Autowired
    private DecryptionService decryptionService;


    public Mono<S3Resource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        String path = pathFromUri(userAction.getUri());
        return decryptionService.getDecryptedCredential(userAction.getCredential())
                .map(cred -> {
                    if (userAction.getCredential().getUsername() == null || userAction.getCredential().getPassword() == null) {
                        Mono.error(new Exception("Invalid"));
                    }
                    System.out.println(userAction.getCredential().getUsername());
                    System.out.println(userAction.getCredential().getPassword());
                    return new UserInfoCredential(userAction.getCredential());
                })
                .map(credential -> new S3Session(URI.create(userAction.getUri()), credential))
                .flatMap(S3Session::initialize)
                .flatMap(s3Session -> s3Session.select(path));

    }

    public String pathFromUri(String uri) {
        String path = "";
        if(uri.contains(ODSConstants.AMAZONS3_URI_SCHEME)){
            path = uri.substring(ODSConstants.AMAZONS3_URI_SCHEME.length() - 1);
        }
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
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
