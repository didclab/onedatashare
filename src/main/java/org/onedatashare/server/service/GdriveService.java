package org.onedatashare.server.service;

import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.GlobusWebClientCredential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.credential.UploadCredential;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.box.BoxSession;
import org.onedatashare.server.module.clientupload.ClientUploadSession;
import org.onedatashare.server.module.dropbox.DbxSession;
import org.onedatashare.server.module.googledrive.GoogleDriveSession;
import org.onedatashare.server.module.gridftp.GridftpSession;
import org.onedatashare.server.module.http.HttpSession;
import org.onedatashare.server.module.vfs.VfsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.onedatashare.server.model.core.ODSConstants.*;

@Service
public class GdriveService implements ResourceService {
    @Autowired
    private UserService userService;

    public Mono<? extends Resource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.getUri());
        String id = userAction.getId();
        ArrayList<IdMap> idMap = userAction.getMap();

        if (userAction.getCredential().isTokenSaved()) {
            return userService.getLoggedInUser(cookie)
                    .map(User::getCredentials)
                    .map(uuidCredentialMap -> uuidCredentialMap.get(UUID.fromString(userAction.getCredential().getUuid())))
                    .map(credential -> new GoogleDriveSession(URI.create(userAction.getUri()), credential))
                    .flatMap(GoogleDriveSession::initialize)
                    .flatMap(driveSession -> driveSession.select(path, id, idMap))
                    .onErrorResume(throwable -> throwable instanceof TokenExpiredException, throwable ->
                            Mono.just(userService.updateCredential(cookie, userAction.getCredential(), ((TokenExpiredException) throwable).cred))
                                    .map(credential -> new GoogleDriveSession(URI.create(userAction.getUri()), credential))
                                    .flatMap(GoogleDriveSession::initialize)
                                    .flatMap(driveSession -> driveSession.select(path, id, idMap))
                    );
        } else {
            return Mono.just(new OAuthCredential(userAction.getCredential().getToken()))
                    .map(oAuthCred -> new GoogleDriveSession(URI.create(userAction.getUri()), oAuthCred))
                    .flatMap(GoogleDriveSession::initializeNotSaved)
                    .flatMap(driveSession -> driveSession.select(path, id, idMap));
        }
    }

    public Mono<Resource> getResourceWithUserActionResource(User userObj, UserActionResource userActionResource) {
        final String path = pathFromUri(userActionResource.getUri());
        String id = userActionResource.getId();
        ArrayList<IdMap> idMap = userActionResource.getMap();
        return Mono.just(userObj)
                .flatMap(user -> createCredential(userActionResource, user))
                .map(credential -> createSession(userActionResource.getUri(), credential))
                .flatMap(session -> {
                    if (session instanceof GoogleDriveSession && !userActionResource.getCredential().isTokenSaved())
                        return ((GoogleDriveSession) session).initializeNotSaved();
                    if (session instanceof BoxSession && !userActionResource.getCredential().isTokenSaved())
                        return ((BoxSession) session).initializeNotSaved();
                    else
                        return session.initialize();
                })
                .flatMap(session -> ((Session) session).select(path, id, idMap));
    }

    public String pathFromUri(String uri) {
        String path = "";
        path = uri.substring(DRIVE_URI_SCHEME.length() - 1);
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public Mono<Credential> createCredential(UserActionResource userActionResource, User user) {
        if (user.isSaveOAuthTokens()) {
            return Mono.just(
                    user.getCredentials().get(
                            UUID.fromString(userActionResource.getCredential()
                                    .getUuid())
                    ));
        }
        else {
            return Mono.just( new OAuthCredential(userActionResource.getCredential().getToken()));
        }
    }

    public Session createSession(String uri, Credential credential) {
            return new GoogleDriveSession(URI.create(uri), credential);
    }

    @Override
    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(Resource::stat);
    }

    @Override
    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::mkdir)
                .flatMap(resource -> ((Resource) resource).stat());
    }

    @Override
    public Mono<Boolean> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::delete)
                .map(v -> true);
    }

    @Override
    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::download);
    }
}
