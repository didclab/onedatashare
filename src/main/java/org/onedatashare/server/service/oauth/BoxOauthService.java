/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.service.oauth;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.*;

import lombok.Getter;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;

/**
 *author: Javier Falca
 */

@Getter
@Configuration
class BoxConfig {
    @Value("${box.clientId}")
    private String clientId;
    @Value("${box.clientSecret}")
    private String clientSecret;
    @Value("${box.redirectUri}")
    private String redirectUri;
    @Value("${box.scope}")
    private String scope;
    @Value("${box.authUri}")
    private String authUri;
}

@Service
public class BoxOauthService{

    @Autowired
    private UserService userService;

    @Autowired
    private BoxConfig boxConfig;

    public String start() {
        String box_redirect = boxConfig.getAuthUri()
                + "?response_type=code"
                + "&client_id=" + boxConfig.getClientId()
                + "&redirect_uri=" + boxConfig.getRedirectUri()
                + "&scope=" + boxConfig.getScope();

        return box_redirect;
    }

    /**
     * @param queryParameters: Access Token returned by Box Authentication using OAuth 2
     * @return OAuthCredential
     */

    public Mono<OAuthCredential> finish(Map<String, String> queryParameters) {
        return Mono.fromSupplier(() -> {
            String code = queryParameters.get("code");
            // Instantiate new Box API connection object
            BoxAPIConnection client = new BoxAPIConnection(boxConfig.getClientId(), boxConfig.getClientSecret(), code);
            OAuthCredential oauth = new OAuthCredential(client.getAccessToken());
            BoxUser user = BoxUser.getCurrentUser(client);
            BoxUser.Info userInfo = user.getInfo();
            oauth.name = "Box: " + userInfo.getLogin();
            oauth.token = client.getAccessToken();
            oauth.refreshToken = client.getRefreshToken();
            Date currentTime = new Date();
            oauth.lastRefresh = new Date(currentTime.getTime());
            oauth.expiredTime = new Date(currentTime.getTime() + client.getExpires());
            return oauth;
        });
    }
}
