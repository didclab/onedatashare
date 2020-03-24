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

import lombok.Data;
import org.apache.log4j.Logger;

/**
 * Logger service for ODS that provides an interface between the application and log4j.
 * Configurations for log4j can be found at src\main\resources\log4j.properties
 */
@Data
public class ODSLoggerService {

    private final static Logger logger = Logger.getRootLogger();

    /**
     * Function that logs DEBUG messages.
     * @param debugMessage - Debug message
     */
    public static void logDebug(String debugMessage){
        logger.debug(debugMessage);
    }

    /**
     * Function that logs INFO level messages
     * @param infoMessage - Info message
     */
    public static void logInfo(String infoMessage){
        logger.info(infoMessage);
    }

    /**
     * Function that logs ERROR level messages.
     * Overloaded method.
     * @param errorMessage - Error message
     */
    public static void logError(String errorMessage){
        logger.error(errorMessage);
    }

    /**
     * Function that logs ERROR level messages.
     * Overloaded method. This method also logs the stack trace by accepting a Throwable.
     * @param errorMessage - Error message
     */
    public static void logError(String errorMessage, Throwable throwable){
        logger.error(errorMessage, throwable);
    }

    /**
     * Function that logs WARN level messages.
     * @param warningMessage - Warning message
     */
    public static void logWarning(String warningMessage){
        logger.warn(warningMessage);
    }
}