package org.onedatashare.server.exceptionHandler;

import com.box.sdk.BoxAPIResponseException;
import org.onedatashare.server.exceptionHandler.error.ODSAccessDeniedException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleUnknownException(Exception exception) {
        exception.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler({ODSAccessDeniedException.class})
    public ResponseEntity<Object> handleOdsAccessDeniedException(ODSAccessDeniedException odsAccessDeniedException) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(odsAccessDeniedException.getMessage());
    }

    @ExceptionHandler({BoxAPIResponseException.class})
    public ResponseEntity<Object> handleBoxAPIResponseException(BoxAPIResponseException boxAPIResponseException) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(boxAPIResponseException.getMessage());
    }
}
