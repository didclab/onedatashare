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

                        ODSLoggerService.logInfo("uploading" + s.length() + " " + state);
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
