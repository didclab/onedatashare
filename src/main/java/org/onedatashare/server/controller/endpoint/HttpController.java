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


package org.onedatashare.server.controller.endpoint;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.service.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/http")
public class HttpController extends EndpointBaseController{
    @Autowired
    private SftpService sftpService;

    @Override
    protected Mono<Stat> listOperation(ListOperation operation) {
        return sftpService.list(operation);
    }

    @Override
    protected Mono<Void> mkdirOperation(MkdirOperation operation) {
        return sftpService.mkdir(operation);
    }

    @Override
    protected Mono<Void> deleteOperation(DeleteOperation operation) {
        return sftpService.delete(operation);
    }

    @Override
    protected Mono<DownloadResponse> downloadOperation(DownloadOperation operation) {
        return sftpService.download(operation).map(DownloadResponse::new);
    }
}