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
import org.onedatashare.server.service.DbxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dropbox")
public class DbxController extends EndpointBaseController{
    @Autowired
    private DbxService dbxService;

    @Override
    protected Stat listOperation(ListOperation operation) {
        return dbxService.list(operation);
    }

    /**
     *
     * @param operation: the id or path in this operation must be the path starting from the root="/" all the way down to the location of folderToCreate property
     * @return
     */
    @Override
    protected ResponseEntity mkdirOperation(MkdirOperation operation) {
        return dbxService.mkdir(operation);
    }

    @Override
    protected ResponseEntity deleteOperation(DeleteOperation operation) {
        return dbxService.delete(operation);
    }

    @Override
    protected DownloadResponse downloadOperation(DownloadOperation operation) {
        return null;
    }
}