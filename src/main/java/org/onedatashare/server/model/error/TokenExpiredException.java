package org.onedatashare.server.model.error;

import org.onedatashare.server.model.credential.OAuthCredential;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

public class TokenExpiredException extends ODSError {

    public OAuthCredential cred;
    public TokenExpiredException(int err, OAuthCredential cred) {
        super("Token Is Expired");
        type = "TokenExpired";
        error = "Token Is Expired.";

        this.cred = cred;
    }
}
