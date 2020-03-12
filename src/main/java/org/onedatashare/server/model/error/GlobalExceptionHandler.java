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


package org.onedatashare.server.model.error;

import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.ODSLoggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This Class provides a deafault method of handling exceptions throughout the project
 * */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Exception handler for Invalid Login
     * @param ilException : Invalid ODS Credential Exception Object
     * */
    @ExceptionHandler(InvalidODSCredentialsException.class)
    public ResponseEntity<String> handle(InvalidODSCredentialsException ilException) {
        ODSLoggerService.logError(ilException.toString());
        return new ResponseEntity<>(ilException.toString(), ilException.status);
    }

    /**
     * Exception handler for unauthorized operation exception
     * @param uException : UnAuthorized Exception Object
     */
    @ExceptionHandler(UnAuthorizedOperationException.class)
    public ResponseEntity<String> handle(UnAuthorizedOperationException uException){
        ODSLoggerService.logError(uException.toString());
        return new ResponseEntity<>(uException.getMessage(), uException.status);
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handle(NotFoundException nfException){
        ODSLoggerService.logError(nfException.toString());
        return new ResponseEntity<>(nfException.getMessage(), nfException.status);
    }

}
