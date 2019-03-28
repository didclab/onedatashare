package org.onedatashare.server.module.googledrive;

import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.onedatashare.server.controller.OauthController;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.IdMap;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import org.onedatashare.server.service.GoogleDriveOauthService;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class GoogleDriveSession  extends Session<GoogleDriveSession, GoogleDriveResource> {
    static GoogleClientSecrets clientSecrets;
    static transient Drive service;
    transient HashMap<String, String> pathToParentIdMap = new HashMap<>();
    ArrayList<IdMap> idMap = null;
    transient LinkedBlockingQueue<String> mkdirQueue = new LinkedBlockingQueue<>();
    //transient final Integer Lock = new Integer(0);


    static String APPLICATION_NAME = "OneDataShare";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/ods");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_READONLY);

    public GoogleDriveSession(URI uri, Credential credential) {
        super(uri, credential);
    }

    //public String redirect_uris = "http://127.0.0.1:8080/api/stork/oauth,https://onedatashare.org/api/stork/oauth,http://127.0.0.1:8080/api/stork/oauth,http://localhost:8080/api/stork/oauth,http:///www.onedatashare.org/api/stork/oauth";
    public static String finishURI = GoogleDriveOauthService.finishURI;


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
            if(credential instanceof OAuthCredential){
                try{
                    service = getDriveService(((OAuthCredential) credential).token);
                    System.out.println("Service: "+service);
                }catch(Throwable t) {
                    throw new RuntimeException(t);
                }
                if(service !=null)
                    s.success(this);
                else {
                        OAuthCredential c = updateToken();
                        s.error(new AuthenticationRequired(401,c, "Bad Request"));
                }
            }
            else s.error(new AuthenticationRequired("oauth"));
        });
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
            //System.out.println("Token1: "+credential.getAccessToken()+"\n Refresh Token1:"+credential.getRefreshToken());
            return credential;

    }

    private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer)  {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                try{
                requestInitializer.initialize(httpRequest);
                httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
                httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
                }catch(IOException e){
                    System.out.println("******IOException********");
                    e.printStackTrace();
                }catch(NullPointerException e){
                    System.out.println("******NullPointerException********");
                    //e.printStackTrace();
                }
            }
        };
    }

    public static Drive getDriveService(String token) throws IOException {
        com.google.api.client.auth.oauth2.Credential credential = authorize(token);
        if(credential!=null){
            return new Drive.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }else {
            return null;
        }
    }

    public  OAuthCredential updateToken(){

        OAuthCredential cred = (OAuthCredential)credential;
        try{
            System.out.println("\nOld AccessToken: "+cred.token+"\n"+cred.refreshToken);
            GoogleDriveOauthService.GoogleDriveConfig c = new GoogleDriveOauthService.GoogleDriveConfig();
            GoogleCredential refreshTokenCredential = new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).setClientSecrets(c.client_id, c.client_secret).build().setRefreshToken(cred.refreshToken);
            if(refreshTokenCredential.refreshToken()){
                System.out.println("REFRESHTOKEN!");
            }; //do not forget to call this
            String accessToken = refreshTokenCredential.getAccessToken();

            cred.token = accessToken;

            System.out.println("New AccessToken:"+cred.token+" RefreshToken:"+cred.refreshToken);
        }catch (IOException e){
            e.printStackTrace();
        }

        return cred;
    }
}
