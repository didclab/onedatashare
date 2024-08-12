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

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.module.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * This will be the service for a user to deploy a Transfer Node on their host.
 * This will allow users to still use the AWS deployment of ODS to talk to the node on their host.
 * Currently I think this will use the C^2 service and ideally it should forward all requests to C^2 and then that service will need to respond to the
 * operation request.
 */
@Service
public class VfsService extends ResourceServiceBase {


    @Override
    protected Resource getResource(String credId) {
        return null;
    }
    @Override
    public Stat list(ListOperation listOperation) {
        return null;
    }

    @Override
    public ResponseEntity mkdir(MkdirOperation mkdirOperation) {
        return null;
    }

    @Override
    public ResponseEntity delete(DeleteOperation deleteOperation) {
        return null;
    }

    @Override
    public DownloadResponse download(DownloadOperation downloadOperation) {
        return null;
    }

}
