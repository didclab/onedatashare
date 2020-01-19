package org.onedatashare.server.model.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.server.model.core.User;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {
    private String email;
    private String token;
    private boolean saveOAuthTokens;
    private boolean isAdmin;
    private boolean compactViewEnabled;

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
