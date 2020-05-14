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

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import lombok.Getter;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
class DbxConfig {
    @Value("${dropbox.key}")
    private String key;
    @Value("${dropbox.secret}")
    private String secret;
    @Value("${dropbox.redirectUri}")
    private String redirectUri;
    @Value("${dropbox.identifier}")
    private String identifier;
}


@Service
public class DbxOauthService  {
    @Autowired
    private DbxConfig dbxConfig;

    @Autowired
    private UserService userService;

    private DbxAppInfo secrets;
    private DbxRequestConfig config;
    private DbxSessionStore sessionStore;
    private DbxWebAuth auth;
    private String token = null;
    private Map<String, String> userTokens = new HashMap<>();

    private static final String STATE = "state", CODE = "code";

    @PostConstruct
    public void DbxOauthServiceInitialize(){
        secrets = new DbxAppInfo(dbxConfig.getKey(), dbxConfig.getSecret());
        config = DbxRequestConfig.newBuilder(dbxConfig.getIdentifier()).build();
        sessionStore = new DbxSessionStore() {
            public void clear() { set(null); }
            public String get() {
//                return token;
                return userTokens.get(userService.getLoggedInUserEmail().block()); }
            public void set(String s) {
                userService.getLoggedInUserEmail()
                        .subscribe(email -> userTokens.put(email, s));
//                token = s;
            }
        };
        auth = new DbxWebAuth(config, secrets);
    }

    public String start(){
        return auth.authorize(DbxWebAuth
                .Request
                .newBuilder()
                .withRedirectUri(dbxConfig.getRedirectUri(), sessionStore)
                .build());
    }

    public Mono<OAuthCredential> finish(Map<String, String> queryParameters) {
        return Mono.fromSupplier(() -> {
            Map<String,String[]> map = new HashMap();
            map.put(STATE, new String[] {queryParameters.get(STATE)});
            map.put(CODE, new String[] {queryParameters.get(CODE)});
            try {
                DbxAuthFinish finish = auth.finishFromRedirect(dbxConfig.getRedirectUri(), sessionStore, map);
                OAuthCredential cred = new OAuthCredential(finish.getAccessToken());
                FullAccount account = new DbxClientV2(config, finish.getAccessToken()).users().getCurrentAccount();
                cred.name = "Dropbox: " + account.getEmail();
                cred.dropboxID = account.getAccountId();
                return cred;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                userService.getLoggedInUserEmail()
                        .subscribe(email -> userTokens.remove(email));
            }
        });
    }
}