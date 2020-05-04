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

import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.googledrive.GDriveSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;

import static org.onedatashare.server.model.core.ODSConstants.DRIVE_URI_SCHEME;

@Service
public class GDriveService extends ResourceService {
    @Autowired
    private UserService userService;

    public Mono<? extends Resource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.getUri());
        String id = userAction.getId();
        ArrayList<IdMap> idMap = userAction.getMap();

        if (userAction.getCredential().isTokenSaved()) {
            return userService.getLoggedInUser(cookie)
                    .handle((usr, sink) -> {
                        this.fetchCredentialsFromUserAction(usr, sink, userAction);
                    })
                    .map(credential -> new GDriveSession(URI.create(userAction.getUri()), (Credential) credential))
                    .flatMap(GDriveSession::initialize)
                    .flatMap(driveSession -> driveSession.select(path, id, idMap))
                    .onErrorResume(throwable -> throwable instanceof TokenExpiredException,
                            throwable -> Mono.just(userService.updateCredential(cookie, userAction.getCredential(),
                                    ((TokenExpiredException) throwable).cred))
                                    .map(credential -> new GDriveSession(URI.create(userAction.getUri()), credential))
                                    .flatMap(GDriveSession::initialize)
                                    .flatMap(driveSession -> driveSession.select(path, id, idMap))
                    );
        } else {
            return Mono.just(new OAuthCredential(userAction.getCredential().getToken()))
                    .map(oAuthCred -> new GDriveSession(URI.create(userAction.getUri()), oAuthCred))
                    .flatMap(GDriveSession::initializeNotSaved)
                    .flatMap(driveSession -> driveSession.select(path, id, idMap));
        }
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

    @Override
    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(Resource::stat);
    }

    @Override
    public Mono<Void> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::mkdir)
                .then();
    }

    @Override
    public Mono<Void> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::delete)
                .then();
    }

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(Resource::download);
    }
}