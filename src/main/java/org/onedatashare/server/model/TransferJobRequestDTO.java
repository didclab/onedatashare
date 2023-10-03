package org.onedatashare.server.model;

import org.onedatashare.server.model.request.UserTransferOptions;

public class TransferJobRequestDTO {
    private String ownerId;
    private FileSource source;
    private FileDestination destination;
    private UserTransferOptions options;
    private String transferNodeName;
}
