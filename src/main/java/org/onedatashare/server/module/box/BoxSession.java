package org.onedatashare.server.module.box;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.box.sdk.BoxAPIConnection;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.service.oauth.BoxOauthService;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BoxSession extends Session<BoxSession, BoxResource> {
    BoxAPIConnection client;
    private transient HashMap<String, String> pathToParentIdMap = new HashMap<>();
    protected ArrayList<IdMap> idMap = null;
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
        this.idMap = idMap;
        if(idMap !=null && idMap.size()>0)
            for (IdMap idPath: idMap) {
                pathToParentIdMap.put(idPath.getPath(),idPath.getId());
            }
        return Mono.just(new BoxResource(this, path,id));
    }

    @Override
    public Mono<BoxSession> initialize() {
        return Mono.create(s -> {
            if(getCredential() instanceof OAuthCredential){
                OAuthCredential oauth = (OAuthCredential) getCredential();
                String client_id = System.getenv("BOX_CLIENT_ID");
                String client_secret = System.getenv("BOX_CLIENT_SECRET");
                client = new BoxAPIConnection(oauth.getToken());
                client.setExpires(oauth.expiredTime.getTime());
                Date time = new Date();
                if(time.before(oauth.expiredTime)){
                    s.success(this);
                }
                else{
//                    Date currentTime = new Date();
//                    oauth.lastRefresh = currentTime;
//                    oauth.expiredTime = new Date(currentTime.getTime() + client.getExpires());
//                    oauth.setToken(client.getAccessToken());
//                    oauth.setRefreshToken(client.getRefreshToken());
                    s.error(new TokenExpiredException(401, oauth));
                }
            }
            else s.error(new AuthenticationRequired("oauth"));

        });
    }
}
