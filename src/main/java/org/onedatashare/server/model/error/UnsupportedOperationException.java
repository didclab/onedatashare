package org.onedatashare.server.model.error;

public class UnsupportedOperationException extends RuntimeException{
    public UnsupportedOperationException() {
        super("Operation is not supported");
    }
}