package org.onedatashare.server.model.requestdata;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionCredential;

@Data
public class SSHCommandData {

    private String type;
    private String host;
    private UserActionCredential credential;
}
