package org.onedatashare.server.model.request;

import lombok.Data;

@Data
public class ChangeRoleRequest {
    private String email;
    private boolean makeAdmin;
}
