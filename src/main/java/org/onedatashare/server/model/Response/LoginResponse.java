package org.onedatashare.server.model.Response;

import lombok.Data;
import org.onedatashare.server.model.core.User;

@Data
public class LoginResponse {
    private String email;
    private String token;
    private boolean saveOAuthTokens;
    private boolean isAdmin;
    private boolean compactViewEnabled;

    private LoginResponse(){}

    public static LoginResponse LoginResponseFromUser(User user, String token){
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.token = token;
        loginResponse.email = user.getEmail();
        loginResponse.compactViewEnabled = user.isCompactViewEnabled();
        loginResponse.isAdmin = user.isAdmin();
        loginResponse.saveOAuthTokens = user.isSaveOAuthTokens();
        return loginResponse;
    }

}
