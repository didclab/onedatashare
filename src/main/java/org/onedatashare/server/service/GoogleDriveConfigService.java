package org.onedatashare.server.service;

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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Service
@Data
public class GoogleDriveConfigService {

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

    private final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/ods");
    private final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_READONLY);
    private final String APPLICATION_NAME = "OneDataShare";
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private FileDataStoreFactory DATA_STORE_FACTORY = getDataStoreFactory();
    private HttpTransport HTTP_TRANSPORT = getHttpTransport();

    public GoogleDriveConfigService() {
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
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, driveClientSecrets, SCOPES)
                    .setDataStoreFactory(DATA_STORE_FACTORY)
                    .build();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private FileDataStoreFactory getDataStoreFactory()
    {
        try {
            return new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpTransport getHttpTransport()
    {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
