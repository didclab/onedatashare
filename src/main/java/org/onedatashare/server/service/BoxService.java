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
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.module.BoxResource;
import org.onedatashare.server.module.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Service
public class BoxService extends ResourceServiceBase {

    @Autowired
    CredentialService credentialService;

    @Override
    protected Resource getResource(String credId) {
        return BoxResource.initialize(credentialService.fetchOAuthCredential(EndpointType.box, credId));
    }

    @Override
    public ResponseEntity delete(DeleteOperation operation) throws IOException {
        return this.getResource(operation.getCredId()).delete(operation);
    }

    @Override
    public Stat list(ListOperation operation) throws IOException {
        return this.getResource(operation.getCredId()).list(operation);
    }

    @Override
    public ResponseEntity mkdir(MkdirOperation operation) throws IOException {
        return this.getResource(operation.getCredId()).mkdir(operation);
    }

    @Override
    public DownloadResponse download(DownloadOperation operation) {return null;}

}

