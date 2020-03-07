package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class InvalidODSCredentialsException extends ODSError{
    public InvalidODSCredentialsException(String reason) {
        super(reason);
        type = "InvalidLogin";
        error = "Invalid ODS email ID / password Combination";
        status = HttpStatus.UNAUTHORIZED;
    }
}
