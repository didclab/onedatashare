package org.onedatashare.server.module.clientupload;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.core.Slice;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

public class ClientUploadSession extends Session<ClientUploadSession,ClientUploadResource> {

    DbxClientV2 client;
    Flux<Slice> flux;
    Long filesize;
    String filename;
    public ClientUploadSession(Flux<Slice> ud, long _filesize, String _filename) {
        super(null, null);
        flux = ud;
        filesize = _filesize;
        filename = _filename;
    }

    @Override
    public Mono<ClientUploadResource> select(String path) {
        return Mono.just(new ClientUploadResource(this, flux));
    }

    @Override
    public Mono<ClientUploadSession> initialize() {
        return Mono.just(this);
    }
}
