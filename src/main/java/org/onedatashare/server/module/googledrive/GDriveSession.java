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


package org.onedatashare.server.module.googledrive;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.service.ODSLoggerService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PUBLIC)
public class GDriveSession extends Session<GDriveSession, GDriveResource> {

    private GDriveConfig driveConfig = GDriveConfig.getInstance();

    private Drive service;
    private transient HashMap<String, String> pathToParentIdMap = new HashMap<>();
    protected ArrayList<IdMap> idMap = null;

    public GDriveSession() {
    }

    public GDriveSession(URI uri, Credential credential) {
        super(uri, credential);
    }

    @Override
    public Mono<GDriveResource> select(String path) {
        return Mono.just(new GDriveResource(this, path));
    }

    @Override
    public Mono<GDriveResource> select(String path, String id, ArrayList<IdMap> idMap) {
        this.idMap = idMap;
        if (idMap != null && idMap.size() > 0)
            for (IdMap idPath : idMap) {
                pathToParentIdMap.put(idPath.getPath(), idPath.getId());
            }
        return Mono.just(new GDriveResource(this, path, id));
    }

    /**
     * This method is used for initializing googleDriveSession when OAuth tokens are not saved in the backend
     * It skips refresh token check as refresh tokens are not stored in the front-end
     */
    public Mono<GDriveSession> initializeNotSaved() {
        return Mono.create(s -> {
            if (getCredential() instanceof OAuthCredential) {
                try {
                    service = getDriveService(((OAuthCredential) getCredential()).token);
                } catch (Throwable t) {
                    s.error(t);
                }
                if (service == null) {
                    ODSLoggerService.logError("Token has expired for the user");
                    s.error(new TokenExpiredException(null, "Invalid token"));
                } else {
                    s.success(this);
                }
            } else
                s.error(new AuthenticationRequired("oauth"));
        });
    }

    @Override
    public Mono<GDriveSession> initialize() {
        return Mono.create(s -> {
            if (getCredential() instanceof OAuthCredential) {
                try {
                    service = getDriveService(((OAuthCredential) getCredential()).token);
                } catch (Throwable t) {
                    s.error(t);
                }
                Date currentTime = new Date();
                if (service != null && ((OAuthCredential) getCredential()).expiredTime != null &&
                        currentTime.before(((OAuthCredential) getCredential()).expiredTime))
                    s.success(this);
                else {
                    OAuthCredential newCredential = updateToken();
                    if (newCredential.refreshToken != null)
                        s.error(new TokenExpiredException(newCredential, "Token has expired"));
                }
            } else s.error(new AuthenticationRequired("oauth"));
        });
    }

    public com.google.api.client.auth.oauth2.Credential authorize(String token) throws IOException {
        OAuthCredential oAuthCredential = (OAuthCredential) this.getCredential();
        com.google.api.client.auth.oauth2.Credential credential = driveConfig.getFlow().loadCredential(token);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(oAuthCredential.getToken());
        tokenResponse.setRefreshToken(oAuthCredential.getRefreshToken());
        tokenResponse.setFactory(JacksonFactory.getDefaultInstance());
        return driveConfig.getFlow().createAndStoreCredential(tokenResponse, "user");
    }

    public static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) {
                try {
                    requestInitializer.initialize(httpRequest);
                    httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
                    httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
                } catch (IOException ioe) {
                    ODSLoggerService.logError("IOException occurred in GoogleDriveSession.setHttpTimeout()", ioe);
                } catch (NullPointerException npe) {
                    ODSLoggerService.logError("IOException occurred in GoogleDriveSession.setHttpTimeout()", npe);
                }
            }
        };
    }

    public Drive getDriveService(String token) throws IOException {
        com.google.api.client.auth.oauth2.Credential credential = authorize(token);
        if (credential == null) {
            return null;
        }
        return new Drive.Builder(
                driveConfig.getHttpTransport(), driveConfig.getJsonFactory(), setHttpTimeout(credential))
                .setApplicationName("OneDataShare")
                .build();
    }

    public OAuthCredential updateToken() {
        // Updating the access token for googledrive using refresh token
        OAuthCredential cred = (OAuthCredential) getCredential();
        try {
            //GoogleCredential refreshTokenCredential = new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).setClientSecrets(c.client_id, c.client_secret).build().setRefreshToken(cred.refreshToken);
            TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                    cred.refreshToken, driveConfig.getClientId(), driveConfig.getClientSecret()).execute();

            cred.token = response.getAccessToken();

            Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
            calendar.add(Calendar.SECOND, response.getExpiresInSeconds().intValue());

            cred.expiredTime = calendar.getTime();

            driveConfig.getFlow().createAndStoreCredential(response, cred.token);
            ODSLoggerService.logInfo("New AccessToken and RefreshToken fetched");
        } catch (com.google.api.client.auth.oauth2.TokenResponseException te) {
            cred.refreshTokenExp = true;
            ODSLoggerService.logError("Refresh token for the user has expired");
        } catch (IOException e) {
            e.printStackTrace();
            ODSLoggerService.logError("IOException in update Token");
        }
        return cred;
    }

}
