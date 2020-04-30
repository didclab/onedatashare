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

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.GlobusWebClientCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.gridftp.GridftpResource;
import org.onedatashare.server.module.gridftp.GridftpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;

@Service
public class GridftpService {

    @Autowired
    private UserService userService;

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction).flatMap(GridftpResource::stat);
    }

    public Mono<GridftpResource> getResourceWithUserUserAction(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.getUri());
        return userService.getLoggedInUser(cookie)
            .flatMap(user -> userService.getGlobusClient(cookie).map(client -> new GlobusWebClientCredential(userAction.getCredential().getGlobusEndpoint(), client)))
            .map(credential -> new GridftpSession(URI.create(userAction.getUri()), credential))
            .flatMap(GridftpSession::initialize)
            .flatMap(GridftpSession -> GridftpSession.select(path));
    }

    public Mono<Void> delete(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::deleteV2)
                .then();
    }

    public Mono<Void> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::mkdir)
                .then();
    }

    public static String pathFromUri(String uri) {
        String path;
        if(uri.contains(ODSConstants.GRIDFTP_URI_SCHEME)){
            path = uri.substring(ODSConstants.GRIDFTP_URI_SCHEME.length());
        }
        else path = uri;
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }
}
