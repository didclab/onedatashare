package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class UnAuthorizedOperationException extends ODSError {
    public UnAuthorizedOperationException(String reason) {
        super(reason);
        this.type = "Unauthorized Access";
        this.status = HttpStatus.FORBIDDEN;
    }
}
