package org.onedatashare.server.model.error;

import org.onedatashare.server.model.credential.OAuthCredential;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

public class TokenExpiredException extends ODSError {

    public OAuthCredential cred;
    public TokenExpiredException(OAuthCredential cred, String message) {
        super(message);
        type = "TokenExpired";
        error = "Token has expired.";
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.cred = cred;
    }
}