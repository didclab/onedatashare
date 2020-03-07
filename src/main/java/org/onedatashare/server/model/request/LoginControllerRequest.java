package org.onedatashare.server.model.request;

import lombok.Data;

@Data
public
class LoginControllerRequest {
    private String email;
    private String password;
    private String confirmPassword;
    private String newPassword;
    private String code;
}