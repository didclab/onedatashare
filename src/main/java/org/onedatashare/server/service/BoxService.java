package org.onedatashare.server.service;


import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.box.BoxResource;
import org.onedatashare.server.module.box.BoxSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class BoxService implements ResourceService<BoxResource> {

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Override
    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction).flatMap(BoxResource::stat);
    }

    @Override
    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction)
                .flatMap(BoxResource::mkdir)
                .flatMap(BoxResource::stat);
    }

    @Override
    public Mono<BoxResource> delete(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction)
                .flatMap(Resource::delete);
    }

    @Override
    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }

    public Mono<BoxResource> getBoxResourceUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.uri);
        String id = userAction.id;
        ArrayList<IdMap> idMap = userAction.map;
        return userService.getLoggedInUser(cookie)
                .map(User::getCredentials)
                .map(uuidCredentialMap -> uuidCredentialMap.get(UUID.fromString(userAction.credential.getUuid())))
                .map(credential -> new BoxSession(URI.create(userAction.uri), credential))
                .flatMap(BoxSession::initialize)
                .flatMap(boxSession -> boxSession.select(path, id, idMap)
                        .onErrorResume(throwable -> throwable instanceof TokenExpiredException, throwable ->
                                Mono.just(userService.updateCredential(cookie,((TokenExpiredException)throwable).cred))
                                        .map(credential -> new BoxSession(URI.create(userAction.uri), credential))
                                        .flatMap(BoxSession::initialize)
                                        .flatMap(driveSession -> driveSession.select(path,id, idMap)))
                );
    }

    public Mono<BoxResource> getBoxResourceUserActionResource(String cookie, UserActionResource userActionResource) {
        final String path = pathFromUri(userActionResource.uri);
        String id = userActionResource.id;
        ArrayList<IdMap> idMap = userActionResource.map;

        return userService.getLoggedInUser(cookie)
                .map(User::getCredentials)
                .map(uuidCredentialMap ->
                        uuidCredentialMap.get(UUID.fromString(userActionResource.credential.getUuid())))
                .map(credential -> new BoxSession(URI.create(userActionResource.uri), credential))
                .flatMap(BoxSession::initialize)
                .flatMap(boxSession -> boxSession.select(path, id, idMap));
    }


    public String pathFromUri(String uri) {
        String path = "";
        if(uri.contains("box://")){
            path = uri.split("box://")[1];
        }
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

}