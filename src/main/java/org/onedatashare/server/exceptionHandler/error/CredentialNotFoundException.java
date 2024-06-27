package org.onedatashare.server.exceptionHandler.error;

import com.onedatashare.commonutils.model.credential.EndpointCredentialType;
public class CredentialNotFoundException extends Exception{
    public CredentialNotFoundException(){
        super("Credential not found for transfer");
    }

    public CredentialNotFoundException(EndpointCredentialType type, String id){
        super(String.format("Credential %s/%s not found",type, id));
    }
}
