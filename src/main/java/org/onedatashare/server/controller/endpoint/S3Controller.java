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
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.GDriveService;
import org.onedatashare.server.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequestMapping("/api/amazons3")
@RestController
public class S3Controller extends EndpointBaseController{
    @Autowired
    private S3Service s3Service;

    /*
    @Override
    protected Mono<Stat> listOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return s3Service.list(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> mkdirOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return s3Service.mkdir(null, userAction).map(this::returnOnSuccess).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> deleteOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return s3Service.delete(null, userAction).map(this::returnOnSuccess).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Stat> uploadOperation() {
        return null;
    }

    @Override
    protected Mono<String> downloadOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return s3Service.download(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Rendering oauthOperation() {
        return null;
    }
     */

    @Override
    protected Mono<Stat> listOperation(ListOperation listOperation) { return s3Service.list(listOperation); }

    @Override
    protected Mono<Void> mkdirOperation(MkdirOperation operation) { return s3Service.mkdir(operation); }

    @Override
    protected Mono<Void> deleteOperation(DeleteOperation deleteOperation) { return s3Service.delete(deleteOperation); }

    @Override
    protected Mono<DownloadResponse> downloadOperation(DownloadOperation downloadOperation) {
        return s3Service.download(downloadOperation).map(DownloadResponse::new);
    }
}