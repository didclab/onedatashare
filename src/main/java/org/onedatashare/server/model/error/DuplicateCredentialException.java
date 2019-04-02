package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class DuplicateCredentialException extends ODSError {
    public DuplicateCredentialException() {
        super("Credential is already registered.");
        type = "NOT_ACCEPTABLE";
        error = "Duplicate Credential";
        status = HttpStatus.NOT_ACCEPTABLE;
    }
}
