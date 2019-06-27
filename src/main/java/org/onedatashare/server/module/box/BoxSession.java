package org.onedatashare.server.module.box;

import com.box.sdk.BoxAPIConnection;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

public class BoxSession extends Session<BoxSession, BoxResource> {
    BoxAPIConnection client;
    public BoxSession(URI uri, Credential credential) {
        super(uri, credential);
    }

    @Override
    public Mono<BoxResource> select(String path){
        return Mono.just(new BoxResource(this, path));
    }


    @Override
    public Mono<BoxResource> select(String path, String id){
        return Mono.just(new BoxResource(this, path));
    }


    @Override
    public Mono<BoxResource> select(String path, String id, ArrayList<IdMap> idMap) {
        return Mono.just(new BoxResource(this, path, id));
    }

    @Override
    public Mono<BoxSession> initialize() {
        return Mono.create(s -> {
            if(credential instanceof OAuthCredential){
                OAuthCredential oauth = (OAuthCredential) credential;
                client = new BoxAPIConnection(oauth.token);
                s.success(this);
            }
            else s.error(new AuthenticationRequired("oauth"));

        });
    }
}
