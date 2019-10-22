package org.onedatashare.server.model.error;
import org.springframework.http.HttpStatus;

public class OldPwdMatchingException extends ODSError{
    public OldPwdMatchingException(String err)
    {
        super(err);
        type = "Invalid Password";
        error = err;
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
