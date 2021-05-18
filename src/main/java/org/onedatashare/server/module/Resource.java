package org.onedatashare.server.module;

import lombok.NoArgsConstructor;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.List;

@NoArgsConstructor
public abstract class Resource {
    protected EndpointCredential credential;

    public Resource(EndpointCredential credential){
        this.credential = credential;
    }

    public abstract Mono<Void> delete(DeleteOperation operation);
    public abstract Mono<Stat> list(ListOperation operation);
    public abstract Mono<Void> mkdir(MkdirOperation operation);
    public String pathFromUrl(String url) throws UnsupportedEncodingException {
        return java.net.URLDecoder.decode(url, "UTF-8");
    }
}
