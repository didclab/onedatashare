package org.onedatashare.server.model.filesystem.exceptions;

public class NoWritePermissionException extends ErrorResponder{
    private static final String EXCEPTION_MESSAGE = "Cannot perform the requested operation : Do not have write " +
            "permission";

    public NoWritePermissionException(String s){
        super(s);
    }

    public NoWritePermissionException(){
        super(EXCEPTION_MESSAGE);
    }
}
