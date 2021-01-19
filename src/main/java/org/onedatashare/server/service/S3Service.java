package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class S3Service extends ResourceService{

    @Autowired
    CredentialService credentialService;

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
//        Mono.just(credentialService.fetchAccountCredential(EndpointType.s3, listOperation.getCredId()).flatMap(accountEndpointCredential -> {
//
//        }))
//        Mono<AccountEndpointCredential> s3Credential = credentialService.fetchAccountCredential(EndpointType.s3, listOperation.getCredId());

        return null;
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return null;
    }

    @Override
    public Mono<Void> delete(DeleteOperation deleteOperation) {
        return null;
    }

    @Override
    public Mono<String> download(DownloadOperation downloadOperation) {
        return null;
    }

    @Override
    protected Mono<? extends Resource> createResource(OperationBase operationBase) {
        return null;
    }
}
