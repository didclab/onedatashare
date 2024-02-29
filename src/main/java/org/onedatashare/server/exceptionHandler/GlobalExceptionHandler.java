package org.onedatashare.server.exceptionHandler;

import com.box.sdk.BoxAPIResponseException;
import com.dropbox.core.DbxException;
import org.apache.log4j.Logger;
import org.onedatashare.server.exceptionHandler.error.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger logger=Logger.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleUnknownException(Exception exception) {
        logger.error("Exception Occurred:",exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler({ODSException.class})
    public ResponseEntity<Object> handleODSException(ODSException odsException) {
        logger.error("ODS Access Denied Exception Occurred:",odsException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(odsException.getMessage());
    }

    @ExceptionHandler({ODSAccessDeniedException.class})
    public ResponseEntity<Object> handleOdsAccessDeniedException(ODSAccessDeniedException odsAccessDeniedException) {
        logger.error("ODS Access Denied Exception Occurred:",odsAccessDeniedException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(odsAccessDeniedException.getMessage());
    }

    @ExceptionHandler({BoxAPIResponseException.class})
    public ResponseEntity<Object> handleBoxAPIResponseException(BoxAPIResponseException boxAPIResponseException) {
        logger.error("Box API Exception Occurred:",boxAPIResponseException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(boxAPIResponseException.getMessage());
    }

    @ExceptionHandler({DbxException.class})
    public ResponseEntity<Object> handleDbxException(DbxException dbxException) {
        logger.error("Dbx Exception Occurred:",dbxException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(dbxException.getMessage());
    }

    @ExceptionHandler({IOException.class})
    public ResponseEntity<Object> handleIOException(IOException ioException) {
        logger.error("IO Exception Occurred:",ioException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ioException.getMessage());
    }

    @ExceptionHandler({InvalidODSCredentialsException.class})
    public ResponseEntity<Object> handleInvalidODSCredentialsException(InvalidODSCredentialsException invalidODSCredentialsException) {
        logger.error("Invalid ODS Credential Exception Occurred:",invalidODSCredentialsException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(invalidODSCredentialsException.getMessage());
    }

    @ExceptionHandler({OldPwdMatchingException.class})
    public ResponseEntity<Object> handleOldPwdMatchingException(OldPwdMatchingException oldPwdMatchingException) {
        logger.error("Old Pwd Matching Exception Occurred:",oldPwdMatchingException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(oldPwdMatchingException.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException notFoundException) {
        logger.error("Not Found Exception Occurred:",notFoundException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(notFoundException.getMessage());
    }

    @ExceptionHandler({UnsupportedEncodingException.class})
    public ResponseEntity<Object> handleUnsupportedEncodingException(UnsupportedEncodingException unsupportedEncodingException) {
        logger.error("Unsupported Encoding Exception Occurred:",unsupportedEncodingException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(unsupportedEncodingException.getMessage());
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException responseStatusException) {
        logger.error("Response Status Exception Occurred:",responseStatusException);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseStatusException.getMessage());
    }
}
