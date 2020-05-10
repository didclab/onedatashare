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


package org.onedatashare.server.module.clientupload;

import com.dropbox.core.v2.DbxClientV2;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.core.Slice;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientUploadSession extends Session<ClientUploadSession,ClientUploadResource> {

    DbxClientV2 client;
    LinkedBlockingQueue<Slice> flux;
    Long filesize;
    String filename;
    public ClientUploadSession(LinkedBlockingQueue<Slice> ud, long _filesize, String _filename) {
        super(null);
        flux = ud;
        filesize = _filesize;
        filename = _filename;
    }

    @Override
    public Mono<ClientUploadResource> select(String path, String id, ArrayList<IdMap> idMap){
        return Mono.just(new ClientUploadResource(this));
    }

    @Override
    public Mono<ClientUploadSession> initialize() {
        return Mono.just(this);
    }
}
