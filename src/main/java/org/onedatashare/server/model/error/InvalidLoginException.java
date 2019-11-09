package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class InvalidLoginException extends ODSError{
    public InvalidLoginException(String reason) {
        super(reason);
        type = "InvalidLogin";
        error = "Invalid Login";
        status = HttpStatus.UNAUTHORIZED;
    }
}
