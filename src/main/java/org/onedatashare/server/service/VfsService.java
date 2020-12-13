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

import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.vfs.VfsResource;
import org.onedatashare.server.module.vfs.VfsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

@Service
public class VfsService extends ResourceService {
    @Autowired
    private UserService userService;

    @Autowired
    private DecryptionService decryptionService;
    
    public Mono<VfsResource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.getUri());
        return userService.getLoggedInUser(cookie)
                .flatMap(user -> {
                    if(userAction.getCredential() == null){
                        // Credentials for FTP will be null
                        return Mono.just(new UserInfoCredential(null));
                    }
                    else {
                        return decryptionService.getDecryptedCredential(userAction.getCredential())
                                .map(userActionCred -> new UserInfoCredential(userActionCred));
                    }
                })
                .map(credential -> {
                    // Encoding the resource URI to avoid errors due to spaces in file/directory names
                    String encodedURI = userAction.getUri();

                    try {
                        encodedURI = URLEncoder.encode(userAction.getUri(), "UTF-8");
                    }
                    catch(UnsupportedEncodingException uee){
                        ODSLoggerService.logError("Exception encountered while encoding input URI");
                        Mono.error(uee);
                    }
                    return new VfsSession(URI.create(encodedURI), credential);
                })
                .flatMap(vfsSession -> vfsSession.initialize())
                .flatMap(vfsSession -> vfsSession.select(path));
    }

    public Mono<VfsResource> getResourceWithUserActionResource(String cookie, UserActionResource userActionResource) {
        final String path = pathFromUri(userActionResource.getUri());
        return userService.getLoggedInUser(cookie)
                .flatMap(user -> {
                    if(userActionResource.getCredential() == null){
                        // Credentials for FTP will be null
                        return Mono.just(new UserInfoCredential(null));
                    }
                    else {
                        return decryptionService.getDecryptedCredential(userActionResource.getCredential())
                                .map(userActionCred -> new UserInfoCredential(userActionCred));
                    }
                })
                .map(credential -> {
                    // Encoding the resource URI to avoid errors due to spaces in file/directory names
                    String encodedURI = userActionResource.getUri();
                    try {
                        encodedURI = URLEncoder.encode(userActionResource.getUri(), "UTF-8");
                    }
                    catch(UnsupportedEncodingException uee){
                        ODSLoggerService.logError("Exception encountered while encoding input URI");
                        Mono.error(uee);
                    }
                    return new VfsSession(URI.create(encodedURI), credential);
                })
                .flatMap(VfsSession::initialize)
                .flatMap(vfsSession -> vfsSession.select(path));
    }


    public String pathFromUri(String path) {
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

    @Override
    protected Mono<? extends Resource> createResource(OperationBase operationBase) {
        return null;
    }

    public Mono<Void> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(VfsResource::mkdir)
                .then();
    }

    public Mono<Void> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(VfsResource::delete)
                .then();
    }

    public Mono<String> download(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(VfsResource::getDownloadURL);
    }

    public Mono<ResponseEntity> getSftpDownloadStream(String cookie, UserActionResource userActionResource) {
        return getResourceWithUserActionResource(cookie, userActionResource).flatMap(VfsResource::sftpObject);
    }
}
