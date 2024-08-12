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
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class DbxOauthService {

    @Autowired
    private DbxConfig dbxConfig;
    private DbxAppInfo secrets;
    private DbxRequestConfig config;
    private DbxSessionStore sessionStore;
    private DbxWebAuth auth;
    private String token = null;

    private static final String STATE = "state", CODE = "code";

    @PostConstruct
    public void DbxOauthServiceInitialize() {
        secrets = new DbxAppInfo(dbxConfig.getKey(), dbxConfig.getSecret());
        config = DbxRequestConfig.newBuilder(dbxConfig.getIdentifier()).build();
        sessionStore = new DbxSessionStore() {
            public void clear() {
                set(null);
            }

            public String get() {
                return token;
            }

            public void set(String s) {
                if (s != null)
                    token = s;
            }
        };
        auth = new DbxWebAuth(config, secrets);
    }

    public String start() {
        return auth.authorize(DbxWebAuth
                .Request
                .newBuilder()
                .withTokenAccessType(TokenAccessType.OFFLINE)
                .withRedirectUri(dbxConfig.getRedirectUri(), sessionStore)
                .build());
    }

    public OAuthEndpointCredential finish(Map<String, String> queryParameters) throws DbxWebAuth.ProviderException, DbxWebAuth.NotApprovedException, DbxWebAuth.BadRequestException, DbxWebAuth.BadStateException, DbxException, DbxWebAuth.CsrfException {
        Map<String, String[]> map = new HashMap<>();
        map.put(STATE, new String[]{queryParameters.get(STATE)});
        map.put(CODE, new String[]{queryParameters.get(CODE)});
        sessionStore.set(queryParameters.get(STATE));
        DbxAuthFinish finish = auth.finishFromRedirect(dbxConfig.getRedirectUri(), sessionStore, map);

        Timestamp timestamp = new Timestamp(Long.valueOf(finish.getExpiresAt()));

        FullAccount account = new DbxClientV2(config, finish.getAccessToken()).users().getCurrentAccount();
        return new OAuthEndpointCredential(account.getEmail())
                .setToken(finish.getAccessToken())
                .setTokenExpires(true)
                .setRefreshToken(finish.getRefreshToken())
                .setRefreshTokenExpires(true)
                .setExpiresAt(new Date(timestamp.getTime()));
    }
}