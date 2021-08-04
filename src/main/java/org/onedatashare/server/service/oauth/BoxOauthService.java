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
import com.box.sdk.BoxUser;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.swing.*;
import java.util.Calendar;
import java.util.Map;

@Service
public class BoxOauthService{


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
    Logger logger = LoggerFactory.getLogger(BoxOauthService.class);

    @Autowired
    private UserService userService;

    public String start() {
        String box_redirect = authUri
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope;

        return box_redirect;
    }

    /**
     * @param queryParameters: Access Token returned by Box Authentication using OAuth 2
     * @return OAuthCredential
     */

    public Mono<OAuthEndpointCredential> finish(Map<String, String> queryParameters) {
        return Mono.create(s -> {
            String code = queryParameters.get("code");
            // Instantiate new Box API connection object
            BoxAPIConnection client = new BoxAPIConnection(clientId, clientSecret, code);
            client.refresh();
            BoxUser user = BoxUser.getCurrentUser(client);
            BoxUser.Info userInfo = user.getInfo();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, Math.toIntExact(client.getExpires()));

            OAuthEndpointCredential credential = new OAuthEndpointCredential(userInfo.getLogin())
                    .setToken(client.getAccessToken())
                    .setTokenExpires(true)
                    .setRefreshToken(client.getRefreshToken())
                    .setRefreshTokenExpires(true)
                    .setExpiresAt(calendar.getTime());
            s.success(credential);
        });
    }
}
