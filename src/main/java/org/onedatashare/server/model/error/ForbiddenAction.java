package org.onedatashare.server.model.error;

public class ForbiddenAction extends ODSError {

    public ForbiddenAction( String err){
        super(err);
        type = "Forbidden Action";
        error = err;
    }

}
