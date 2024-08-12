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


package org.onedatashare.server.exceptionHandler.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ODSError {
  public NotFoundException() {
    super("Not Found");
    type = "NotFound";
    error = "Not Found";
    status = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public NotFoundException(String message){
    super(message);
    type = "NotFound";
    error = message;
    status = HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
