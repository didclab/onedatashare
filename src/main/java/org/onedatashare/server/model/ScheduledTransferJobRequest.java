package org.onedatashare.server.model;

import lombok.Data;
import org.onedatashare.server.model.request.UserTransferOptions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ScheduledTransferJobRequest implements Serializable {

    LocalDateTime jobStartTime;
    UUID jobUuid;
    String ownerId;
    FileSource source;
    FileDestination destination;
    UserTransferOptions options;
    String transferNodeName;
}
