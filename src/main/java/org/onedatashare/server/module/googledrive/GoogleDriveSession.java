package org.onedatashare.server.module.googledrive;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import reactor.core.publisher.Mono;

import org.onedatashare.server.service.GoogleDriveOauthService;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class GoogleDriveSession  extends Session<GoogleDriveSession, GoogleDriveResource> {
    static GoogleClientSecrets clientSecrets;
    transient Drive service;
    transient HashMap<String, String> pathToParentIdMap = new HashMap<>();
    transient LinkedBlockingQueue<String> mkdirQueue = new LinkedBlockingQueue<>();
    transient final Integer Lock = new Integer(0);

    static String APPLICATION_NAME = "OneDataShare";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/ods");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_READONLY);

    protected GoogleDriveSession(URI uri, Credential credential) {
        super(uri, credential);
    }

    @Override
    public Mono<GoogleDriveResource> select(String path) {
        return null;
    }

    @Override
    protected Mono<GoogleDriveSession> initialize() {
        return null;
    }


    public static void initGoogle() {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        GoogleDriveOauthService.GoogleDriveConfig c = new GoogleDriveOauthService.GoogleDriveConfig();
        List<String> redirect_uris;

        if (c != null && c.client_id != null && c.client_secret != null && c.redirect_uris != null) {
            redirect_uris = Arrays.asList(c.redirect_uris.replaceAll("\\[|\\]|\"|\n","")
                    .trim()
                    .split(","));
            String finishURI = redirect_uris.get(0);

            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();

            details.setAuthUri(c.auth_uri).setClientId(c.client_id)
                    .setClientSecret(c.client_secret).setRedirectUris(Arrays.asList(finishURI))
                    .setTokenUri(c.token_uri);
            clientSecrets = new GoogleClientSecrets().setInstalled(details);

        }
    }

    public static com.google.api.client.auth.oauth2.Credential authorize(String token) throws IOException {
        // Load client secrets.
        initGoogle();

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .build();
        com.google.api.client.auth.oauth2.Credential credential = flow.loadCredential(token);
        return credential;
    }

    private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                requestInitializer.initialize(httpRequest);
                httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
                httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
            }
        };
    }

    public static Drive getDriveService(String token) throws IOException {
        com.google.api.client.auth.oauth2.Credential credential = authorize(token);
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
