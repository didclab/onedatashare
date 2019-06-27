package org.onedatashare.server.model.error;


import org.onedatashare.server.service.ODSLoggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NullPointerException.class)
    public String handle(NullPointerException npe) {
        ODSLoggerService.logError("Nullpointer Exception thrown", npe);
        return "NullPointerException";
    }

    //Handles generic and unknown exceptions
    @ExceptionHandler(Exception.class)
    public String handle(Exception e) {
        ODSLoggerService.logError("Unknown Exception thrown", e);
        return "Exception";
    }

    @ExceptionHandler(UnsupportedOperation.class)
    public ResponseEntity<UnsupportedOperation> handle(UnsupportedOperation uo){
        return new ResponseEntity<>(uo, uo.status);
    }
}
