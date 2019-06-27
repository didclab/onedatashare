package org.onedatashare.server.service.oauth;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.*;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 *author: Javier Falca
 */

@Service
public class BoxOauthService {

    @Autowired
    private UserService userService;


    public static class BoxConfig {

        public static String client_id = "7xifcu329elepngu76j3k5vart312ibr";
        public static String client_secret = "UPGc0WGVHTNB6UmYU2RPneXPA2yJB5Qp";
        public static String redirect_uri = "http://127.0.0.1:8080/api/stork/oauth/box";
        public static String box_redirect = "https://account.box.com/api/oauth2/authorize";
        public static String scope = "root_readwrite";
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
                 System.out.println("Runtime exception occurred while finishing initializing Box oauth session");
                 throw new RuntimeException(e);
             }

    }

    public class BoxClient extends BoxAPIConnection{

        public BoxClient(String accessToken) {

            super(accessToken);
        }
    }

}
