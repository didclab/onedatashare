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


package org.onedatashare.server.module.box;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponseException;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.service.ODSLoggerService;
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

    /**
     * This method is used for initializing boxSession when OAuth tokens are not saved in the backend
     * It skips refresh token check as refresh tokens are not stored in the front-end
     */
    public Mono<BoxSession> initializeNotSaved() {
        return Mono.create(s -> {
            if (getCredential() instanceof OAuthCredential) {
                try {
                    client = new BoxAPIConnection(((OAuthCredential) getCredential()).getToken());
                } catch (Throwable t) {
                    s.error(t);
                }
                if (client == null) {
                    ODSLoggerService.logError("Token has expired for the user");
                    s.error(new TokenExpiredException(null, "Invalid token"));
                } else {
                    s.success(this);
                }
            } else
                s.error(new AuthenticationRequired("oauth"));
        });
    }

    @Override
    public Mono<BoxSession> initialize() {
        return Mono.create(s -> {
            if(getCredential() instanceof OAuthCredential){
                OAuthCredential oauth = (OAuthCredential) getCredential();
                try{
                    //String client_id = System.getenv("BOX_CLIENT_ID");
                    //String client_secret = System.getenv("BOX_CLIENT_SECRET");
                    client = new BoxAPIConnection(oauth.getToken());
                    client.setExpires(oauth.expiredTime.getTime());
                    Date time = new Date();
                    if(time.before(oauth.expiredTime)){
                        s.success(this);
                    }else{
                        ODSLoggerService.logError("Box Token Expiration.");
//                        s.error(new TokenExpiredException(401, oauth));
                    }
                }catch(BoxAPIResponseException e){
                    ODSLoggerService.logError("Box API Exception");
                    s.error(new TokenExpiredException(oauth, "Box API Exception"));
                }catch(Exception e){
                    ODSLoggerService.logError("Box Other Exception");
                    e.printStackTrace();
                    s.error(new AuthenticationRequired("oauth"));
                }
            }
            else{
                s.error(new AuthenticationRequired("oauth"));
            }
        });
    }
}
