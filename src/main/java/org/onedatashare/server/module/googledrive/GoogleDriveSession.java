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
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PUBLIC)
public class GoogleDriveSession  extends Session<GoogleDriveSession, GoogleDriveResource> {

    @Autowired
    GoogleDriveConfig driveConfig;

    private Drive service;
    private transient HashMap<String, String> pathToParentIdMap = new HashMap<>();
    protected ArrayList<IdMap> idMap = null;

    public GoogleDriveSession(){}

    public GoogleDriveSession(URI uri, Credential credential) {
        super(uri, credential);
    }

    @Override
    public Mono<GoogleDriveResource> select(String path) {
        return Mono.just(new GoogleDriveResource(this, path));
    }
    @Override
    public Mono<GoogleDriveResource> select(String path, String id, ArrayList<IdMap> idMap) {
        this.idMap = idMap;
        if(idMap !=null && idMap.size()>0)
            for (IdMap idPath: idMap) {
                pathToParentIdMap.put(idPath.getPath(),idPath.getId());
            }
        return Mono.just(new GoogleDriveResource(this, path,id));
    }

    @Override
    public Mono<GoogleDriveSession> initialize() {
        return Mono.create(s -> {
            if(getCredential() instanceof OAuthCredential){
                try {
                    service = getDriveService(((OAuthCredential) getCredential()).token);
                } catch (Throwable t) {
                    s.error(t);
                }
                Date currentTime = new Date();
                if(service !=null && ((OAuthCredential) getCredential()).expiredTime != null &&
                        currentTime.before(((OAuthCredential) getCredential()).expiredTime))
                    s.success(this);
                else {
                    OAuthCredential newCredential = updateToken();
                    s.error(new TokenExpiredException(401, newCredential));
                }
            }
            else s.error(new AuthenticationRequired("oauth"));
        });
    }

    public com.google.api.client.auth.oauth2.Credential authorize(String token) throws IOException {
        // Load client secrets.
        if(driveConfig == null)
            driveConfig = new GoogleDriveConfig();
        com.google.api.client.auth.oauth2.Credential credential = driveConfig.getFlow().loadCredential(token);
        return credential;
    }

    private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer)  {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) {
                try{
                requestInitializer.initialize(httpRequest);
                httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
                httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
                }
                catch(IOException ioe){
                    ODSLoggerService.logError("IOException occurred in GoogleDriveSession.setHttpTimeout()", ioe);
                }
                catch(NullPointerException npe){
                    ODSLoggerService.logError("IOException occurred in GoogleDriveSession.setHttpTimeout()", npe);
                }
            }
        };
    }

    public Drive getDriveService(String token) throws IOException {
        com.google.api.client.auth.oauth2.Credential credential = authorize(token);
        if(credential == null){
            return null;
        }
        return new Drive.Builder(
            driveConfig.getHttpTransport(), driveConfig.getJSON_FACTORY(), setHttpTimeout(credential))
            .setApplicationName(driveConfig.getApplicationName())
            .build();
    }

    public OAuthCredential updateToken(){
        // Updating the access token for googledrive using refresh token
        OAuthCredential cred = (OAuthCredential) getCredential();
        try{
            ODSLoggerService.logInfo("Old AccessToken: "+cred.token+"\tRefresh token: "+cred.refreshToken);
            //GoogleCredential refreshTokenCredential = new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).setClientSecrets(c.client_id, c.client_secret).build().setRefreshToken(cred.refreshToken);
            TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                    cred.refreshToken, driveConfig.getClientId(), driveConfig.getClientSecret()).execute();

            cred.token = response.getAccessToken();

            Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
            calendar.add(Calendar.SECOND, response.getExpiresInSeconds().intValue());

            cred.expiredTime = calendar.getTime();

            driveConfig.getFlow().createAndStoreCredential(response, cred.token);
            ODSLoggerService.logInfo("New AccessToken:"+response.getAccessToken()+" RefreshToken:"+cred.refreshToken);
        }catch (IOException e){
            e.printStackTrace();
        }

        return cred;
    }
}
