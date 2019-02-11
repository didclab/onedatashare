package org.onedatashare.server.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Slice;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Flux;
@Data
@Document
public class UploadCredential extends Credential {
    @Transient
    private Flux<Slice> fux;
    @Transient
    private String name;
    @Transient
    private Long size;

    public UploadCredential(Flux<Slice> fax,  Long _size, String _name){
        fux = fax;
        name = _name;
        size = _size;
    }
}