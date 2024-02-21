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
import org.onedatashare.server.exceptionHandler.error.TokenExpiredException;
import org.onedatashare.server.model.filesystem.exceptions.ErrorMessage;
import org.onedatashare.server.model.filesystem.exceptions.ErrorResponder;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.response.DownloadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public abstract class EndpointBaseController {
    Logger logger = LoggerFactory.getLogger(EndpointBaseController.class);
    
    @GetMapping("/ls")
    public Stat list(@RequestParam String credId, @RequestParam Optional<String> path,
                           @RequestParam Optional<String> identifier) throws IOException {
        ListOperation operation = ListOperation.builder()
                .credId(credId)
                .path(path.orElse(""))
                .id(identifier.orElse(""))
                .build();
        logger.info("CredId: " + credId + " path: " + path + " id: " + identifier);
        return listOperation(operation);
    }

    @PostMapping("/mkdir")
    public ResponseEntity mkdir(@RequestBody MkdirOperation operation) throws IOException {
        logger.info(operation.toString());
        mkdirOperation(operation);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/rm")
    public ResponseEntity delete(@RequestBody DeleteOperation operation) throws IOException {
        logger.info(operation.toString());
        deleteOperation(operation);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/download")
    public DownloadResponse download(@RequestBody DownloadOperation operation){
        return downloadOperation(operation);
    }

    protected abstract Stat listOperation(ListOperation listOperation) throws IOException;
    protected abstract ResponseEntity<String> mkdirOperation(MkdirOperation operation) throws IOException;
    protected abstract ResponseEntity<String> deleteOperation(DeleteOperation deleteOperation) throws IOException;
    protected abstract DownloadResponse downloadOperation(DownloadOperation downloadOperation);

    @ExceptionHandler(ErrorResponder.class)
    public ResponseEntity<ErrorMessage> handle(ErrorResponder errorResponder) {
        return new ResponseEntity<>(errorResponder.getError(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handle(TokenExpiredException tokenExpiredException) {
        return new ResponseEntity<>(tokenExpiredException.toString(), tokenExpiredException.status);
    }
}
