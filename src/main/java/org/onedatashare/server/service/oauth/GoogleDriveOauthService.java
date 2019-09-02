package org.onedatashare.server.service.oauth;


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
import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.module.googledrive.GoogleDriveSession;
import org.onedatashare.server.service.GoogleDriveConfigService;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Service
public class GoogleDriveOauthService{

    @Autowired
    private UserService userService;

    @Autowired
    private GoogleDriveConfigService driveConfigService;

    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/ods");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE);
    private static final HttpTransport HTTP_TRANSPORT;
    private static final FileDataStoreFactory DATA_STORE_FACTORY;

    static{
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @PostConstruct
    public void initClientSecrets() {


    }

    private String getUrl() {
        String url;
        try {
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY,  driveConfigService.getDriveClientSecrets(), SCOPES)
                            .setAccessType("offline").setApprovalPrompt("force")
                            .setDataStoreFactory(DATA_STORE_FACTORY)
                            .build();

            AuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(driveConfigService.getRedirectUri()).setState(flow.getClientId());

            url = authorizationUrl.toURL().toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    public synchronized String start() {
        GoogleClientSecrets cs = driveConfigService.getDriveClientSecrets();
        GoogleClientSecrets.Details dt = cs.getDetails();

        if (driveConfigService.getRedirectUri() == null)
            throw new RuntimeException("Google Drive config missing");
        return getUrl();
    }

    private OAuthCredential storeCredential(String code) {
        try {
            GoogleAuthorizationCodeFlow flow = driveConfigService.getFlow();
            // Build flow and trigger user authorization request.
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(driveConfigService.getRedirectUri()).execute();

            OAuthCredential oauth = new OAuthCredential(response.getAccessToken());
            oauth.refreshToken = response.getRefreshToken();


            Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
            calendar.add(Calendar.SECOND, response.getExpiresInSeconds().intValue());

            oauth.expiredTime = calendar.getTime();

            ODSLoggerService.logInfo("TokenStore " + oauth.getToken());
            ODSLoggerService.logInfo("Refresh Token: " +oauth.refreshToken);
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

