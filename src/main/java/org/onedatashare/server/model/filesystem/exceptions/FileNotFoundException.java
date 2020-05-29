package org.onedatashare.server.model.filesystem.exceptions;

public class FileNotFoundException extends ErrorResponder{
    private static final String ERROR_MESSAGE = "Requested file couldn't be found";

    public FileNotFoundException(){
        super(ERROR_MESSAGE);
    }

    public FileNotFoundException(String message){
        super(message);
    }
}
