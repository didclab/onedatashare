package org.onedatashare.server.service;

import com.onedatashare.commonutils.model.credential.EndpointCredentialType;
import com.onedatashare.commonutils.service.auth.CredentialService;
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

import java.io.IOException;

@Service
public class S3Service extends ResourceServiceBase {

    @Autowired
    CredentialService credentialService;

    private static final EndpointCredentialType ENDPOINT_TYPE = EndpointCredentialType.s3;


    @Override
    protected Resource getResource(String credId) {
        return S3Resource.initialize(credentialService.fetchAccountCredential(EndpointCredentialType.s3, credId));
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
