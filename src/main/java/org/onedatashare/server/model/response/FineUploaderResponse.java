package org.onedatashare.server.model.response;

import lombok.Data;

@Data
public class FineUploaderResponse {
    private boolean success;
    private boolean error;

    public static FineUploaderResponse ok(){
        FineUploaderResponse fineUploaderResponse = new FineUploaderResponse();
        fineUploaderResponse.error = false;
        fineUploaderResponse.success = true;
        return fineUploaderResponse;
    }

    public static FineUploaderResponse error(){
        FineUploaderResponse fineUploaderResponse = new FineUploaderResponse();
        fineUploaderResponse.error = true;
        fineUploaderResponse.success = false;
        return fineUploaderResponse;
    }

}
