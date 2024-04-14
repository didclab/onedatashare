package org.onedatashare.server.module;

import com.onedatashare.commonservice.model.credential.EndpointCredential;
import lombok.NoArgsConstructor;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@NoArgsConstructor
public abstract class Resource {
    protected EndpointCredential credential;

    protected Resource(EndpointCredential credential){
        this.credential = credential;
    }

    public abstract ResponseEntity delete(DeleteOperation operation) throws IOException;
    public abstract Stat list(ListOperation operation) throws IOException;
    public abstract ResponseEntity mkdir(MkdirOperation operation) throws IOException;
    public abstract String download(DownloadOperation operation);

    public String pathFromUrl(String url) throws UnsupportedEncodingException {
        return java.net.URLDecoder.decode(url, "UTF-8");
    }
}
