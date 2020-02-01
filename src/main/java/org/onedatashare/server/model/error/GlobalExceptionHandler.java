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

}
