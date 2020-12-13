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
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.UnsupportedOperationException;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.http.HttpResource;
import org.onedatashare.server.module.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;

@Service
public class HttpFileService extends ResourceService {
    @Autowired
    private UserService userService;

    public Mono<HttpResource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        String path = pathFromUri(userAction.getUri());
        return userService.getLoggedInUser(cookie)
                .map(user -> new HttpSession(URI.create(userAction.getUri())))
                .flatMap(HttpSession::initialize)
                .flatMap(httpSession -> httpSession.select(path));
    }

    private String pathFromUri(String uri) {
        String path = "";
        if(uri.startsWith(ODSConstants.HTTPS_URI_SCHEME) || uri.startsWith(ODSConstants.HTTP_URI_SCHEME))
            path = uri;

        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(HttpResource::stat);
    }

    /* Not allowed */
    public Mono<Void> mkdir(String cookie, UserAction userAction) {
        throw new UnsupportedOperationException();
    }

    /* Not allowed */
    public Mono<Void> delete(String cookie, UserAction userAction) {
        throw new UnsupportedOperationException();
    }

    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
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

    @Override
    protected Mono<? extends Resource> createResource(OperationBase operationBase) {
        return null;
    }
}
