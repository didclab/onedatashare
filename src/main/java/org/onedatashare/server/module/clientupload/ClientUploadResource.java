package org.onedatashare.server.module.clientupload;


import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Slice;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.core.Tap;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;


public class ClientUploadResource extends Resource<ClientUploadSession, ClientUploadResource> {

//    public Mono<ClientUploadResource> select(String path) {
//        return Mono.just(new ClientUploadResource());
//    }
    public ClientUploadResource(ClientUploadSession session){
        super(session, null);
    }

    @Override
    public Mono<Stat> stat(){
        Stat s = new Stat();
        s.file = true;
        s.dir = false;
        s.size = session.filesize;
        s.name = session.filename;
        return Mono.just(s);
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

        @Override
        public Stat getTransferStat() {
            return null;
        }

        public Flux<Slice> tap(long size) {
            System.out.println("Inside tap()");
            return Flux.generate(() -> session.filesize,
                (state, sink) -> {
                    try{
                        Slice s = session.flux.take();
                        sink.next(s);

                        System.out.println("uploading" + s.length() + " " + state);
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
            return null;
        }
    }
}
