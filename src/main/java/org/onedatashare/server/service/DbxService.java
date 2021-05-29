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
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;
import org.onedatashare.server.service.oauth.ResourceServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.beans.factory.annotation.Value;
import org.onedatashare.server.module.DropboxResource;
import org.onedatashare.server.module.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
public class DbxService extends ResourceServiceBase {

    @Autowired
    private CredentialService credentialService;

    @Value("${dropbox.identifier}")
    private String DROPBOX_CLIENT_IDENTIFIER;

    public String pathFromDbxUri(String uri) {
        String path = "";
        if(uri.contains(ODSConstants.DROPBOX_URI_SCHEME)){
            path = uri.substring(ODSConstants.DROPBOX_URI_SCHEME.length() - 1);
        }
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    protected Mono<? extends Resource> getResource(String credId) {
        return credentialService.fetchOAuthCredential(EndpointType.dropbox, credId)
                .flatMap(credential -> DropboxResource.initialize(credential, DROPBOX_CLIENT_IDENTIFIER))
                .subscribeOn(Schedulers.elastic());
    }

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return this.getResource(listOperation.getCredId()).
                flatMap(resource -> resource.list(listOperation));
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return this.getResource(mkdirOperation.getCredId()).
                flatMap(resource -> resource.mkdir(mkdirOperation));
    }

    @Override
    public Mono<Void> delete(DeleteOperation deleteOperation) {
        return this.getResource(deleteOperation.getCredId()).
                flatMap(resource -> resource.delete(deleteOperation));
    }

    @Override
    public Mono<String> download(DownloadOperation downloadOperation) {
        return null;
    }

    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return null;
    }
}
