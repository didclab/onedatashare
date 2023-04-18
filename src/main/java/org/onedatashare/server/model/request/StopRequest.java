package org.onedatashare.server.model.request;

import lombok.Data;

@Data
public class StopRequest {
    Long jobId;
    String transferNodeName;

}

