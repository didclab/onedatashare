package org.onedatashare.server.service;


import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.module.googledrive.GoogleDriveSession;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

public class GoogleDriveOauthService implements OauthService{

    public String key;
    private static String finishURI;
    private static GoogleClientSecrets clientSecrets;
    @Value("${drive_client_id}")
    private static String clientid;
    @Value("${drive_client_secret}")
    private static String cSecrets;
    @Value("${drive_project_id}")
    private static String pid;
    @Value("${drive_auth_uri}")
    private static String authuri;
    @Value("${drive_token_uri}")
    private static String turi;
    @Value("${drive_auth_provider_x509_cert_url}")
    private static String authProvider;
    @Value("${drive_redirect_uris}")
    private static String ruri;
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/ods");
    private static final FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE);

    public static class GoogleDriveConfig {
        public String client_id, client_secret, auth_uri, token_uri, auth_provider_x509_cert_url, project_id, redirect_uris;

        public GoogleDriveConfig() {
            this.client_id = clientid;
            this.client_secret = cSecrets;
            this.auth_uri = authuri;
            this.token_uri = turi;
            this.auth_provider_x509_cert_url = authProvider;
            this.project_id = pid;
            this.redirect_uris = ruri;
        }
    }

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        GoogleDriveConfig c = new GoogleDriveConfig();
        List<String> redirect_uris;

        if (c == null || c.client_id == null || c.client_secret == null || c.token_uri == null || c.redirect_uris == null)
            finishURI = null;
        else {
            redirect_uris = Arrays.asList(c.redirect_uris.replaceAll("\\[|\\]|\"|\n","")
                    .trim()
                    .split(","));
            finishURI = redirect_uris.get(0);

            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();

            details.setAuthUri(c.auth_uri).setClientId(c.client_id)
                    .setClientSecret(c.client_secret).setRedirectUris(Arrays.asList(finishURI))
                    .setTokenUri(c.token_uri);
            clientSecrets = new GoogleClientSecrets().setInstalled(details);
        }
    }

    private static String getUrl() {
        String url;
        try {
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                            .setDataStoreFactory(DATA_STORE_FACTORY)
                            .build();

            AuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(finishURI).setState(flow.getClientId());

            url = authorizationUrl.toURL().toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    public synchronized String start() {
        this.key = clientSecrets.getDetails().getClientId();
        if (finishURI == null)
            throw new RuntimeException("Google Drive config missing");
        return getUrl();
    }

    private static String storeCredential(String code) {
        String accessToken;
        //String userId = "user";
        try {
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                            .setDataStoreFactory(DATA_STORE_FACTORY)
                            .build();

            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(finishURI).execute();

            accessToken = response.getAccessToken();

            flow.createAndStoreCredential(response, accessToken);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return accessToken;
    }

    public synchronized OAuthCredential finish(String token) {
        String accessToken = storeCredential(token);
        try {
            Drive service = GoogleDriveSession.getDriveService(accessToken);
            String userId = service.about().get().setFields("user").execute().getUser().getEmailAddress();
            OAuthCredential cred = new OAuthCredential(accessToken);
            cred.name = "GoogleDrive: " + userId;
            cred.token = userId;
            return cred;
        } catch (Exception e) {
            System.out.println("Runtime exception occurred while finishing initializing Google drive oauth session");
            throw new RuntimeException(e);
        }
    }
}
