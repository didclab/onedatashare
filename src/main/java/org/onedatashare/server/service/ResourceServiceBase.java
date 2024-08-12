package org.onedatashare.server.service;

import com.dropbox.core.DbxException;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.module.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

public abstract class ResourceServiceBase {

    protected abstract Resource getResource(String credId);

    public abstract ResponseEntity delete(DeleteOperation operation) throws IOException, DbxException;
    public abstract Stat list(ListOperation operation) throws IOException;
    public abstract ResponseEntity mkdir(MkdirOperation operation) throws IOException, DbxException;
    public abstract DownloadResponse download(DownloadOperation operation);

}
