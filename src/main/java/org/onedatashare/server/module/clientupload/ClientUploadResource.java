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


import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Slice;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.core.Tap;
import org.onedatashare.server.service.ODSLoggerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource class that provides services for file upload initiated by client
 */
public class ClientUploadResource extends Resource<ClientUploadSession, ClientUploadResource> {

    public ClientUploadResource(ClientUploadSession session){
        super(session, null);
    }

    @Override
    public Mono<Stat> stat(){
        Stat stat = new Stat();
        stat.setFile(true);
        stat.setDir(false);
        stat.setSize(getSession().filesize);
        stat.setName(getSession().filename);
        return Mono.just(stat);
    }

    @Override
    public Mono<Stat> getTransferStat() {
        Stat uploadStat = new Stat();
        uploadStat.setSize(getSession().filesize);
        uploadStat.setDir(false);
        uploadStat.setFile(true);
        uploadStat.setName(getSession().filename);

        Stat tapstat = new Stat();
        tapstat.setSize(getSession().filesize);
        List<Stat> filestat = new ArrayList<Stat>();
        filestat.add(uploadStat);
        tapstat.setFilesList(filestat);

        return Mono.just(tapstat);
    }

    @Override
    public Mono<ClientUploadResource> select(String path) {
        return null;
    }
    @Override
    public Tap tap(){
        return new ClientUploadTap();
    }

    public class ClientUploadTap implements Tap{
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();

        public Flux<Slice> tap(long size) {
            return Flux.generate(() -> getSession().filesize,
                (state, sink) -> {
                    try{
                        Slice s = getSession().flux.take();
                        sink.next(s);

                        if(state - s.length() == 0){
                            sink.complete();
                        }
                        return state -  s.length();
                    }catch(Exception e){
                        return state;
                    }
                });
        }

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
            return this.tap(1<<10);
        }
    }
}
