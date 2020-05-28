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
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.DbxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/dropbox")
public class DbxController extends EndpointBaseController{
    @Autowired
    private DbxService dbxService;

    @Override
    protected Mono<Stat> listOperation(ListOperation listOperation) {
        return null;
    }

    @Override
    protected Mono<Void> mkdirOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return dbxService.mkdir(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Void> deleteOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return dbxService.delete(null, userAction).subscribeOn(Schedulers.elastic());
    }
    
    @Override
    protected Mono<String> downloadOperation(RequestData requestData){
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return dbxService.download(null, userAction).subscribeOn(Schedulers.elastic());
    }
}