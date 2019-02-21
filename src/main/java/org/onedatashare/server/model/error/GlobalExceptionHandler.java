package org.onedatashare.server.model.error;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NullPointerException.class)
    public String handle(NullPointerException e) {
        System.out.println("Nullpointer Exception thrown");
        e.printStackTrace();
        return "NullPointerException";
    }

    //Handles generic and unknown exceptions
    @ExceptionHandler(Exception.class)
    public String handle(Exception e) {
        System.out.println("Unknown Exception thrown");
        e.printStackTrace();
        return "Exception";
    }

    @ExceptionHandler(UnsupportedOperation.class)
    public ResponseEntity<UnsupportedOperation> handle(UnsupportedOperation uo){
        return new ResponseEntity<>(uo, uo.status);
    }
}
