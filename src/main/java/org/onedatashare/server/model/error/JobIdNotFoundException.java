package org.onedatashare.server.model.error;

public class JobIdNotFoundException extends Exception{

    public JobIdNotFoundException(){
        super("Job Id not found in MetaData service");
    }
}
