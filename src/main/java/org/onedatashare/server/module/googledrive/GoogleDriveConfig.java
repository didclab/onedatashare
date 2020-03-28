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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Data
public class GoogleDriveConfig {

    @Value("${drive.auth.uri}")
    private String authUri;

    @Value("${drive.token.uri}")
    private String tokenUri;

    @Value("${drive.auth.provider.x509.cert.uri}")
    private String authProviderX509CertUrl;

    @Value("${drive.redirect.uri}")
    private String redirectUri;

    private String clientId, clientSecret, projectId;
    private GoogleClientSecrets driveClientSecrets;
    private GoogleAuthorizationCodeFlow flow;

    private final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_READONLY);
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/ods");
    private FileDataStoreFactory dataStoreFactory;
    private HttpTransport httpTransport;

    public GoogleDriveConfig() {
        clientId = System.getenv("ods_drive_client_id");
        clientSecret = System.getenv("ods_drive_client_secret");
        projectId = System.getenv("ods_drive_project_id");

        if (getClientId() != null || getClientSecret() != null || getTokenUri() != null || getRedirectUri() != null){
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();

            details.setAuthUri(authUri).setClientId(clientId)
                    .setClientSecret(clientSecret).setRedirectUris(Arrays.asList(redirectUri))
                    .setTokenUri(tokenUri);
            driveClientSecrets = new GoogleClientSecrets().setInstalled(details);
        }

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        try {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, driveClientSecrets, SCOPES)
                    .setDataStoreFactory(dataStoreFactory)
                    .build();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
