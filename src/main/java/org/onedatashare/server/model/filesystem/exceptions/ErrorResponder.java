package org.onedatashare.server.model.filesystem.exceptions;

public abstract class ErrorResponder extends Exception{
    protected ErrorMessage errorMessage;

    protected ErrorResponder(String message){
        this.errorMessage = new ErrorMessage(message);
    }

    public ErrorMessage getError(){
        return this.errorMessage;
    }
}
