package org.onedatashare.server.service;

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.module.FtpResource;
import org.onedatashare.server.module.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Service
public class FtpService extends ResourceServiceBase {

    @Autowired
    private CredentialService credentialService;

    @Override
    protected Resource getResource(String credId) {
        return FtpResource.initialize(credentialService.fetchAccountCredential(EndpointType.ftp, credId));
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
