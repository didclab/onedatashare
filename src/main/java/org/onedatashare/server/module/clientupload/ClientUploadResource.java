package org.onedatashare.server.module.clientupload;


import org.onedatashare.server.model.core.Slice;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ClientUploadResource{

//    public Mono<ClientUploadResource> select(String path) {
//        return Mono.just(new ClientUploadResource());
//    }

    public ClientUploadTap tap(){
        return new ClientUploadTap();
    }

    public class ClientUploadTap{
        public Flux<Slice> tap(Mono<FilePart> filePart) {
            System.out.println("Inside tap()");
            return Flux.generate(
                    () -> 0L,
                    (state, sink) -> {
                        System.out.println("Inside Flux.generate()");
                        filePart.flatMapMany(data -> data.content())
                                .map(x -> x.asByteBuffer())
                                .subscribe(dataBuffer -> {
                                    sink.next(new Slice(dataBuffer.array()));
                                    sink.complete();
                                });
                        return state + 1;
                    });

        }

    }
}
