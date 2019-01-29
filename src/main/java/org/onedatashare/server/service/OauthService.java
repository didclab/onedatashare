package org.onedatashare.server.service;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import org.onedatashare.server.controller.OauthController;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.reactive.result.view.RedirectView;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static reactor.core.publisher.Mono.just;

@Service
public class OauthService {
  @Value("${dropbox.key}")
  private String key;

  @Value("${dropbox.secret}")
  private String secret;

  @Value("${dropbox.redirect}")
  private String finishURI;

  @Autowired
  private UserService userService;

  @Autowired
  private OauthService oauthService;

  private DbxAppInfo secrets;

  private DbxRequestConfig config;

  private DbxSessionStore sessionStore;

  private DbxWebAuth auth;

  public synchronized String start() {
    if (secrets == null) {
      throw new RuntimeException("Dropbox OAuth is disabled.");
//    } if (auth != null) {
//      throw new IllegalStateException("Don't call this twice.");
    } try {
      auth = new DbxWebAuth(config, secrets);
      // Authorize the DbxWebAuth auth as well as redirect the user to the finishURI, done this way to appease OAuth 2.0
      return auth.authorize(DbxWebAuth.Request.newBuilder().withRedirectUri(finishURI, sessionStore).build());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  //
  public synchronized Mono<OAuthCredential> finish(String token, String cookie) {
    Map<String,String[]> map = new HashMap();
    map.put("state", new String[] {this.key});
    map.put("code", new String[] {token});

    try {
      DbxAuthFinish finish = auth.finishFromRedirect(finishURI, sessionStore, map);
      OAuthCredential cred = new OAuthCredential(finish.getAccessToken());
      FullAccount account = new DbxClientV2(config, finish.getAccessToken()).users().getCurrentAccount();
      cred.name = "Dropbox: " + account.getEmail();
      cred.dropboxID = account.getAccountId();
       return userService.getCredentials(cookie).flatMap(val -> {
         for (Credential value: val.values()) {
           OAuthCredential oauthVal = ((OAuthCredential) value);
           if ((oauthVal.dropboxID.equals(cred.dropboxID))) { //Checks if the ID already matches
             return Mono.empty();           //Account already exists
           }
         }
         return Mono.just(cred);            //Account is not in the database, store as new
       });
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
