package org.onedatashare.server.service;

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.module.Resource;
import org.onedatashare.server.module.S3Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class S3Service extends ResourceServiceBase {

    @Autowired
    CredentialService credentialService;

    private static final EndpointType ENDPOINT_TYPE = EndpointType.s3;


    @Override
    protected Mono<? extends Resource> getResource(String credId) {
        return credentialService.fetchAccountCredential(EndpointType.s3, credId)
                .flatMap(S3Resource::initialize)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return this.getResource(operation.getCredId()).flatMap(resource -> resource.delete(operation));
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return this.getResource(operation.getCredId()).flatMap(resource -> resource.list(operation));
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return this.getResource(operation.getCredId()).flatMap(resource -> resource.mkdir(operation));
    }

    @Override
    public Mono<String> download(DownloadOperation operation) {
        return null;
    }

}
