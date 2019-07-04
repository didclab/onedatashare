package org.onedatashare.server.module.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

public class DbxSession extends Session<DbxSession, DbxResource> {
  private DbxClientV2 client;

  protected DbxClientV2 getClient(){
    return client;
  }

  public DbxSession(URI uri, Credential cred) {
    super(uri, cred);
  }

  @Override
  public Mono<DbxResource> select(String path) {
    return Mono.just(new DbxResource(this, path));
  }

  @Override
  public Mono<DbxResource> select(String path, String id) {
    return Mono.just(new DbxResource(this, path));
  }

  @Override
  public Mono<DbxResource> select(String path, String id, ArrayList<IdMap> idMap) {
    return Mono.just(new DbxResource(this, path));
  }
  @Override
  public Mono<DbxSession> initialize() {
    return Mono.create(s -> {
      if(getCredential() instanceof OAuthCredential){
        OAuthCredential oauth = (OAuthCredential) getCredential();
        DbxRequestConfig config =
                DbxRequestConfig.newBuilder("OneDataShare-DIDCLab").build();
        client = new DbxClientV2(config, oauth.token);
        s.success(this);
      }
      else s.error(new AuthenticationRequired("oauth"));
    });
  }
}
