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
