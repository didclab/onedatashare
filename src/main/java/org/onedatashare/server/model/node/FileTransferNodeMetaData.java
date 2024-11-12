package org.onedatashare.server.model.node;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class FileTransferNodeMetaData implements Serializable {
    //ods metrics
    String odsOwner;
    String nodeName;
    UUID nodeUuid;
    Boolean runningJob;
    Boolean online;
    Long jobId;
    UUID jobUuid;

}