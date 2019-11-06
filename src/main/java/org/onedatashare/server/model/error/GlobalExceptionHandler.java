package org.onedatashare.server.model.error;

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
     * Exception handler for Invalid Login i.e., unauthorized use of the service
     * @param ilException : Invalid Exception Object
     * */
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<InvalidLoginException> handle(InvalidLoginException ilException){
        ODSLoggerService.logError(ilException.toString());
        return new ResponseEntity<>(ilException, ilException.status);
    }

}
