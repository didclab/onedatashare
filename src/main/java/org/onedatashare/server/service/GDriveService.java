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

import com.google.api.services.drive.Drive;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.googledrive.GDriveConfig;
import org.onedatashare.server.module.googledrive.GDriveResource;
import org.onedatashare.server.module.googledrive.GDriveSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.onedatashare.server.model.core.ODSConstants.GDRIVE_URI_SCHEME;

@Service
public class GDriveService extends ResourceService {
    @Autowired
    private UserService userService;

    @Autowired
    private CredentialService credentialService;
    private static final EndpointType ENDPOINT_TYPE = EndpointType.gdrive;

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return this.createResource(listOperation).flatMap(Resource::stat);
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return createResource(mkdirOperation)
                .flatMap(Resource::mkdir)
                .then();
    }

    @Override
    public Mono<Void> delete(DeleteOperation deleteOperation) {
        return this.createResource(deleteOperation)
                .flatMap(Resource::delete)
                .then();
    }

    @Override
    public Mono<String> download(DownloadOperation downloadOperation) {
        return this.createResource(downloadOperation)
                .flatMap(Resource::download);
    }

    protected Mono<? extends Resource> createResource(OperationBase operationBase) {
        GDriveConfig driveConfig = GDriveConfig.getInstance();
        return credentialService.fetchOAuthCredential(this.ENDPOINT_TYPE, operationBase.getCredId())
                .map(oAuthEndpointCredential -> driveConfig.getDriveService(oAuthEndpointCredential))
                .map(drive -> new GDriveResource((Drive) drive, operationBase.getPath(), operationBase.getId()))
                .subscribeOn(Schedulers.elastic());
    }
}