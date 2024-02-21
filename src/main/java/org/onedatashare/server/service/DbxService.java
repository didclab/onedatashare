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
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.response.DownloadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.beans.factory.annotation.Value;
import org.onedatashare.server.module.DropboxResource;
import org.onedatashare.server.module.Resource;

import java.io.IOException;

@Service
public class DbxService extends ResourceServiceBase {

    @Autowired
    private CredentialService credentialService;

    @Value("${dropbox.identifier}")
    private String DROPBOX_CLIENT_IDENTIFIER;

    protected Resource getResource(String credId) {
        return DropboxResource.initialize(credentialService.fetchOAuthCredential(EndpointType.dropbox, credId),
                DROPBOX_CLIENT_IDENTIFIER);
    }

    @Override
    public Stat list(ListOperation listOperation) throws IOException {
        return this.getResource(listOperation.getCredId()).list(listOperation);
    }

    @Override
    public ResponseEntity mkdir(MkdirOperation mkdirOperation) throws IOException {
        return this.getResource(mkdirOperation.getCredId()).mkdir(mkdirOperation);
    }

    @Override
    public ResponseEntity delete(DeleteOperation deleteOperation) throws IOException {
        return this.getResource(deleteOperation.getCredId()).delete(deleteOperation);
    }

    @Override
    public DownloadResponse download(DownloadOperation downloadOperation) {
        return null;
    }

}
