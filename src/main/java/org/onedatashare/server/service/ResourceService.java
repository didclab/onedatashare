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
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.useraction.UserAction;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.Map;
import java.util.UUID;

public abstract class ResourceService {
    public abstract Object list(ListOperation listOperation);
    public abstract Mono<Void> mkdir(MkdirOperation mkdirOperation);
    public abstract Mono<Void> delete(DeleteOperation deleteOperation);
    public abstract Mono<String> download(DownloadOperation downloadOperation);

    protected abstract Mono<? extends Resource> createResource(OperationBase operationBase);

    protected void fetchCredentialsFromUserAction(User usr, SynchronousSink sink, UserAction userAction){
        if(userAction.getCredential() == null || userAction.getCredential().getUuid() == null) {
            sink.error(new AuthenticationRequired("oauth"));
        }
        Map credMap = usr.getCredentials();
        Credential credential = (Credential) credMap.get(UUID.fromString(userAction.getCredential().getUuid()));
        if(credential == null){
            sink.error(new NotFoundException("Credentials for the given UUID not found"));
        }
        sink.next(credential);
    }
}
