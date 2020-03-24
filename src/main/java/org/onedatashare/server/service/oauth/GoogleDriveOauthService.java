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


import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.module.googledrive.GoogleDriveConfig;
import org.onedatashare.server.module.googledrive.GoogleDriveSession;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Service
public class GoogleDriveOauthService{

    @Autowired
    private UserService userService;

    @Autowired
    private GoogleDriveConfig driveConfig;

    private final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE);

    private String getUrl() {
        String url;
        try {
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                            driveConfig.getHttpTransport(), driveConfig.getJSON_FACTORY(),
                            driveConfig.getDriveClientSecrets(), SCOPES)
                            .setAccessType("offline").setApprovalPrompt("force")
                            .setDataStoreFactory(driveConfig.getDataStoreFactory())
                            .build();

            AuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(driveConfig.getRedirectUri()).setState(flow.getClientId());

            url = authorizationUrl.toURL().toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    public synchronized String start() {
        GoogleClientSecrets cs = driveConfig.getDriveClientSecrets();
        GoogleClientSecrets.Details dt = cs.getDetails();

        if (driveConfig.getRedirectUri() == null)
            throw new RuntimeException("Google Drive config missing");
        return getUrl();
    }

    private OAuthCredential storeCredential(String code) {
        try {
            GoogleAuthorizationCodeFlow flow = driveConfig.getFlow();
            // Build flow and trigger user authorization request.
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(driveConfig.getRedirectUri()).execute();

            OAuthCredential oauth = new OAuthCredential(response.getAccessToken());
            oauth.refreshToken = response.getRefreshToken();


            Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
            calendar.add(Calendar.SECOND, response.getExpiresInSeconds().intValue());

            oauth.expiredTime = calendar.getTime();

            flow.createAndStoreCredential(response, oauth.token);
            return oauth;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Mono<OAuthCredential> finish( String token, String cookie) {
        OAuthCredential oauth = storeCredential(token);
        try {
            Drive service = new GoogleDriveSession().getDriveService(oauth.getToken());
            String userId = service.about().get().setFields("user").execute().getUser().getEmailAddress();

            oauth.name = "GoogleDrive: " + userId;
            return userService.getCredentials(cookie).flatMap(val -> {
                for (Credential value : val.values()) {
                    OAuthCredential oauthVal = ((OAuthCredential) value);
                    if ((oauthVal.name != null && oauthVal.name.equals(oauth.name))) { //Checks if the ID already matches
                        return Mono.empty(); //Account already exists
                    }
                }
                return Mono.just(oauth);
            });
        } catch (Exception e) {
            ODSLoggerService.logError("Runtime exception occurred while finishing initializing Google drive oauth session");
            throw new RuntimeException(e);
        }
    }
}

