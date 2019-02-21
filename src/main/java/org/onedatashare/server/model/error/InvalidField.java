package org.onedatashare.server.model.error;

public class InvalidField extends ODSError {

    public InvalidField( String err){
        super(err);
        type = "Invalid Field";
        error = err;
    }

}
