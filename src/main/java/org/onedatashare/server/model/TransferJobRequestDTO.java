package org.onedatashare.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.request.UserTransferOptions;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferJobRequestDTO implements Serializable {
    private String ownerId;
    private FileSource source;
    private FileDestination destination;
    private UserTransferOptions options;
    private String transferNodeName;

}
