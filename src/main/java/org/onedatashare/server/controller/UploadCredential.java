package org.onedatashare.server.controller;

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

@Data
@Document
public class UploadCredential extends Credential {
    @Transient
    private Queue<Slice> fux;
    @Transient
    private String name;
    @Transient
    private Long size;
    @Transient
    private Mono<FilePart> _no1;

    public UploadCredential(Queue<Slice> fax, Long _size, String _name){
        fux = fax;
        name = _name;
        size = _size;
    }
}