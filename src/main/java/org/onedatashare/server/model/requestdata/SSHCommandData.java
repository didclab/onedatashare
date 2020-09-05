package org.onedatashare.server.model.requestdata;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionCredential;

@Data
public class SSHCommandData {
    private String host;
    private UserActionCredential credential;
    private String commandWithPath;
    private String port;
}
