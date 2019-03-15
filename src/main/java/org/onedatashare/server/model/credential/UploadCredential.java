package org.onedatashare.server.model.credential;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Slice;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class UploadCredential extends Credential {
    @Transient
    private LinkedBlockingQueue<Slice> fux;
    @Transient
    private String name;
    @Transient
    private Long size;
    @Transient
    private Mono<FilePart> _no1;

    public UploadCredential(LinkedBlockingQueue<Slice> fax, Long _size, String _name){
        fux = fax;
        name = _name;
        size = _size;
    }
}