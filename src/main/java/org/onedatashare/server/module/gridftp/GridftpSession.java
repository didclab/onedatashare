package org.onedatashare.server.module.gridftp;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.GlobusWebClientCredential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

public class GridftpSession extends Session<GridftpSession, GridftpResource> {
    public GlobusClient client;
    EndPoint endpoint;
    public GridftpSession(URI uri, Credential cred) {
        super(uri, cred);
    }

    @Override
    public Mono<GridftpResource> select(String path) {
        return Mono.just(new GridftpResource(this, path));
    }

    @Override
    public Mono<GridftpResource> select(String path, String id) {
        return Mono.just(new GridftpResource(this, path));
    }

    @Override
    public Mono<GridftpResource> select(String path, String id, ArrayList<IdMap> idMap) {
        return Mono.just(new GridftpResource(this, path));
    }

    @Override
    public Mono<GridftpSession> initialize() {
        return Mono.create(s -> {
            if(credential instanceof GlobusWebClientCredential){
                client = ((GlobusWebClientCredential) credential)._globusClient;
                endpoint = ((GlobusWebClientCredential) credential)._endpoint;
                s.success(this);
            }
            else s.error(new AuthenticationRequired("gridftp"));
        });
    }
}
