/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.service;


import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.box.BoxResource;
import org.onedatashare.server.module.box.BoxSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;

@Service
public class BoxService extends ResourceService {

    @Autowired
    private UserService userService;

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction).flatMap(BoxResource::stat);
    }

    public Mono<Void> mkdir(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction)
                .flatMap(BoxResource::mkdir)
                .then();
    }

    public Mono<Void> delete(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction)
                .flatMap(BoxResource::delete)
                .then();
    }

    public Mono<String> download(String cookie, UserAction userAction) {
        return getBoxResourceUserActionUri(cookie, userAction)
                .flatMap(BoxResource::download);
    }

    public Mono<BoxResource> getBoxResourceUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.getUri());
        String id = userAction.getId();
        ArrayList<IdMap> idMap = userAction.getMap();
        if (userAction.getCredential().isTokenSaved()) {
            return userService.getLoggedInUser(cookie)
                    .handle((usr, sink) -> {
                        this.fetchCredentialsFromUserAction(usr, sink, userAction);
                    })
                    .map(credential -> new BoxSession(URI.create(userAction.getUri()), (Credential) credential))
                    .flatMap(BoxSession::initialize)
                    .flatMap(boxSession -> boxSession.select(path, id, idMap))
                    .onErrorResume(throwable -> throwable instanceof TokenExpiredException, throwable -> {
                        userService.deleteBoxCredential(cookie, userAction.getCredential(), ((TokenExpiredException) throwable).cred).subscribe();
                        return null;
                    });
        } else {
            return Mono.just(new OAuthCredential(userAction.getCredential().getToken()))
                    .map(oAuthCred -> new BoxSession(URI.create(userAction.getUri()), oAuthCred))
                    .flatMap(BoxSession::initializeNotSaved)
                    .flatMap(boxSession -> boxSession.select(path, id, idMap));
        }
    }

    public String pathFromUri(String uri) {
        String path = "";
        if(uri.contains(ODSConstants.BOX_URI_SCHEME)){
            path = uri.substring(ODSConstants.BOX_URI_SCHEME.length() - 1);
        }
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
        }

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return null;
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return null;
    }

    @Override
    public Mono<Void> delete(DeleteOperation deleteOperation) {
        return null;
    }

    @Override
    public Mono<String> download(DownloadOperation downloadOperation) {
        return null;
    }
}

