package org.onedatashare.server.model.response;

import lombok.Data;

@Data
public class ShellCommandResponse {
    private String output;
    private String error;
}
