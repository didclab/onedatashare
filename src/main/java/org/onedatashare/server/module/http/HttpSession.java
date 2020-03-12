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


package org.onedatashare.server.module.http;

import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

public class HttpSession extends Session<HttpSession, HttpResource> {
    public HttpSession(URI uri) {
        super(uri);
    }

    @Override
    /*
    * Ignoring path and idMap (Not used)
     */
    public Mono<HttpResource> select(String path, String id, ArrayList<IdMap> idMap) {
        return Mono.just(new HttpResource(this, getUri().toString()));
    }

    @Override
    public Mono<HttpSession> initialize() {
        return Mono.create(s-> s.success(this));
    }

    @Override
    public Mono<HttpResource> select(String path){
        return Mono.just(new HttpResource(this, getUri().toString()));
    }
}
