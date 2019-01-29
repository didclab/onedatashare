package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class UnsupportedOperation extends ODSError {
    public UnsupportedOperation() {
        super("Operation is not supported");
        type = "UnsupportedOperation";
        error = "Operation is not supported";
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
