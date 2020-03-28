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

import static org.onedatashare.server.model.core.ODSConstants.DROPBOX_CLIENT_IDENTIFIER;

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
                DbxRequestConfig.newBuilder(DROPBOX_CLIENT_IDENTIFIER).build();
        client = new DbxClientV2(config, oauth.token);
        s.success(this);
      }
      else s.error(new AuthenticationRequired("oauth"));
    });
  }
}
