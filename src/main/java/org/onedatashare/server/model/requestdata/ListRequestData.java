package org.onedatashare.server.model.requestdata;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionCredential;

@Data
public class ListRequestData {
    private String uri;
    private String id;
    private String portNumber;
    private String type;
    private UserActionCredential credential;
}
