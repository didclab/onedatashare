package org.onedatashare.server.model.filesystem.exceptions;

public class FileAlreadyExistsException extends ErrorResponder{
    private static final String ERROR_MESSAGE = "File already exists";

    public FileAlreadyExistsException(){
        super(ERROR_MESSAGE);
    }

    public FileAlreadyExistsException(String s){
        super(s);
    }

}
