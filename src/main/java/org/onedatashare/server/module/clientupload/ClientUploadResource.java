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
        public Flux<Slice> tap(long size) {
            System.out.println("Inside tap()");
            return Flux.generate(() -> 0L,
                (state, sink) -> {
                    if(session.flux.isEmpty()){
                        return state;
                    }
                    Slice s = session.flux.poll();
                    sink.next(s);
                    if(state + s.length() == session.filesize){
                        sink.complete();
                    }
                    return state + s.length();
                });
        }
    }
}
