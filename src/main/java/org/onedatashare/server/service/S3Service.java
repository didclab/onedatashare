package org.onedatashare.server.service;

import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.ftp.FtpResource;
import org.onedatashare.server.module.s3.S3Resource;
import org.onedatashare.server.module.s3.S3Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URI;


@Service
public class S3Service extends ResourceService {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private DecryptionService decryptionService;

    @Autowired
    private CredentialService credentialService;


    public Mono<S3Resource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        String path = pathFromUri(userAction.getUri());
        return decryptionService.getDecryptedCredential(userAction.getCredential())
                .map(cred -> {
                    if (userAction.getCredential().getUsername() == null || userAction.getCredential().getPassword() == null) {
                        Mono.error(new Exception("Invalid"));
                    }
                    return new UserInfoCredential(userAction.getCredential());
                })
                .map(credential -> new S3Session(URI.create(path), credential))
                .flatMap(S3Session::initialize)
                .flatMap(s3Session -> s3Session.select(path));

    }

    public String pathFromUri(String uri) {
        String path = uri.replace(ODSConstants.AMAZONS3_URI_SCHEME, "amazons3/");
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

    public Mono<Boolean> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(S3Resource::mkdir)
                .map(x -> true);
    }

    public Mono<Boolean> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(S3Resource::delete)
                .map(x -> true);
    }

    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    public Mono<String> download(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie,userAction)
                .flatMap(S3Resource::generateDownloadLink).subscribeOn(Schedulers.elastic());
    }

    private static final EndpointType ENDPOINT_TYPE = EndpointType.s3;

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return this.createResource(listOperation).flatMap(Resource::stat);
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return createResource(mkdirOperation)
                .flatMap(Resource::mkdir)
                .then();
    }

    @Override
    public Mono<Void> delete(DeleteOperation deleteOperation) {
        return this.createResource(deleteOperation)
                .flatMap(Resource::delete)
                .then();
    }

    @Override
    public Mono<String> download(DownloadOperation downloadOperation) {
        return this.createResource(downloadOperation)
                .flatMap(Resource::download);
    }

//    @Override
//    protected Mono<? extends Resource> createResource(OperationBase operationBase) {
//        GDriveConfig driveConfig = GDriveConfig.getInstance();
//        return credentialService.fetchAccountCredential(this.ENDPOINT_TYPE, operationBase.getCredId())
//                .map(oAuthEndpointCredential -> driveConfig.getDriveService(oAuthEndpointCredential))
//                .map(drive -> new GDriveResource((Drive) drive, operationBase.getPath(), operationBase.getId()))
//                .subscribeOn(Schedulers.elastic());
//
//    }

    protected Mono<? extends Resource> createResource(OperationBase operationBase) {
        return credentialService.fetchAccountCredential(this.ENDPOINT_TYPE, operationBase.getCredId())
                .flatMap(S3Resource::initialize);
    }

}