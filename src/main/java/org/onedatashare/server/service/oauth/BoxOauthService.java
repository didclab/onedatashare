package org.onedatashare.server.service.oauth;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.*;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 *author: Javier Falca
 */

@Service
public class BoxOauthService {

    @Autowired
    private UserService userService;


    public static class BoxConfig {

        private static String client_id = System.getenv("BOX_CLIENT_ID");
        private static String client_secret = System.getenv("BOX_CLIENT_SECRET");
        private static String scope = "root_readwrite";
        private static String redirect_uri = System.getenv("BOX_REDIRECT_URI");
        private static String box_redirect = "https://account.box.com/api/oauth2/authorize";

    }

    public synchronized String start() {

        String box_redirect = BoxConfig.box_redirect
                + "?response_type=code"
                + "&client_id=" + BoxConfig.client_id
                + "&redirect_uri=" + BoxConfig.redirect_uri
                + "&scope=" + BoxConfig.scope;

        return box_redirect;
    }

    /**
     * @param code: Access Token returned by Box Authentication using OAuth 2
     * @return OAuthCredential
     */

    public synchronized Mono<OAuthCredential> finish(String code, String cookie) {

        // Instantiate new Box API connection object
        BoxAPIConnection client = new BoxAPIConnection(BoxConfig.client_id, BoxConfig.client_secret, code);
        OAuthCredential oauth = new OAuthCredential(client.getAccessToken());
        BoxUser user = BoxUser.getCurrentUser(client);
        BoxUser.Info userInfo = user.getInfo();
        oauth.name = "Box: " + userInfo.getLogin();
        oauth.token = client.getAccessToken();
        oauth.refreshToken = client.getRefreshToken();
        Date currentTime = new Date();
        oauth.lastRefresh = new Date(currentTime.getTime());
        oauth.expiredTime = new Date(currentTime.getTime() + client.getExpires());
        try{
            return userService.getCredentials(cookie).flatMap(val -> {
                for (Credential value : val.values()) {
                    OAuthCredential oauthVal = ((OAuthCredential) value);
                    if ((oauthVal.name != null && oauthVal.name.equals(oauth.name))) { //Checks if the ID already matches
                        return Mono.empty(); //Account already exists
                    }
                }
                return Mono.just(oauth);
            });
        } catch(Exception e) {
            ODSLoggerService.logError("Runtime exception occurred while finishing initializing Box oauth session");
            throw new RuntimeException(e);
        }

    }

}
