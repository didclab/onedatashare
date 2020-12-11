package org.onedatashare.server.model.response;

import lombok.Data;

@Data
public class TransferJobSubmittedResponse {
    String id;
    int status;
    String message;
}
