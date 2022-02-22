package org.onedatashare.server.model.error;

public class UnknownUserId extends Exception{

    public UnknownUserId(){
        super("The userId supplied is not known");
    }
}
