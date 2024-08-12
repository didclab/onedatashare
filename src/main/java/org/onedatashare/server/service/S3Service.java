package org.onedatashare.server.service;

import com.dropbox.core.DbxException;
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.module.Resource;
import org.onedatashare.server.module.S3Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Service
public class S3Service extends ResourceServiceBase {

    @Autowired
    CredentialService credentialService;

    private static final EndpointType ENDPOINT_TYPE = EndpointType.s3;


    @Override
    protected Resource getResource(String credId) {
        return S3Resource.initialize(credentialService.fetchAccountCredential(EndpointType.s3, credId));
    }

    @Override
    public ResponseEntity delete(DeleteOperation operation) throws IOException {
        return this.getResource(operation.getCredId()).delete(operation);
    }

    @Override
    public Stat list(ListOperation operation) throws IOException {
        return this.getResource(operation.getCredId()).list(operation);
    }

    @Override
    public ResponseEntity mkdir(MkdirOperation operation) throws IOException {
        return this.getResource(operation.getCredId()).mkdir(operation);
    }

    @Override
    public DownloadResponse download(DownloadOperation operation) {
        return null;
    }

}
