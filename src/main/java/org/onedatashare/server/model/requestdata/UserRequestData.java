package org.onedatashare.server.model.requestdata;

import lombok.Data;

@Data
public class UserRequestData {
    private String action;
    private String email;
    private String firstName;
    private String lastName;
    private String organization;
    private String password;
    private String uri;
    private String uuid;
    private String code;
    private String confirmPassword;
    private String newPassword;
    private boolean saveOAuth;
    private boolean isAdmin;
    private boolean compactViewEnabled;

    private int pageNo;
    private int pageSize;
    private String sortBy;
    private String sortOrder;
    private String captchaVerificationValue;
}