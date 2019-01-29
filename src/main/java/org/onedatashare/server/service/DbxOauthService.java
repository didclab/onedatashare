package org.onedatashare.server.service;

import com.dropbox.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.RedirectView;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class DbxOauthService  {

    @Value("${dropbox.key}")
    private String key;

    @Value("${dropbox.secret}")
    private String secret;

    @Value("${dropbox.redirect}")
    private String finishURI;

    private DbxAppInfo secrets;

    private DbxRequestConfig config;

    private DbxSessionStore sessionStore;

    private DbxWebAuth auth;

    public synchronized String start() {
        if (secrets == null) {
            throw new RuntimeException("Dropbox OAuth is disabled.");
        } if (auth != null) {
            //throw new IllegalStateException("Don't call this twice.");
        } try {
            auth = new DbxWebAuth(config, secrets);
            // Authorize the DbxWebAuth auth as well as redirect the user to the finishURI, done this way to appease OAuth 2.0
            return auth.authorize(DbxWebAuth.Request.newBuilder().withRedirectUri(finishURI, sessionStore).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized OAuthCredential finish(String token) {
        Map<String,String[]> map = new HashMap();
        map.put("state", new String[] {this.key});
        map.put("code", new String[] {token});

        try {
            DbxAuthFinish finish = auth.finishFromRedirect(finishURI, sessionStore, map);
            OAuthCredential cred = new OAuthCredential(finish.getAccessToken());
            cred.name = "Dropbox";
            return cred;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public RedirectView redirectToDropboxAuth(Boolean value) {
        String url = start();
        return new RedirectView(url);
    }

    @PostConstruct
    public void postConstructInit(){
        secrets = new DbxAppInfo(this.key, this.secret);
        config = DbxRequestConfig.newBuilder("OneDataShare-DIDCLab").build();
        sessionStore = new DbxSessionStore() {
            public void clear() { set(null); }
            public String get() { return key; }
            public void set(String s) { key = s; }
        };
    }
}
