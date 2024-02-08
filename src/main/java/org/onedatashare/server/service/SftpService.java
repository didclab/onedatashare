package org.onedatashare.server.service;

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.module.SftpResource;
import org.onedatashare.server.module.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SftpService extends ResourceServiceBase {
    @Autowired
    private CredentialService credentialService;


    @Override
    protected Mono<? extends Resource> getResource(String credId) {
        return credentialService.fetchAccountCredential(EndpointType.sftp, credId)
                .flatMap(SftpResource::initialize)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return getResource(operation.getCredId())
                .flatMap(resource -> resource.delete(operation));
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return getResource(operation.getCredId())
                .flatMap(resource -> resource.list(operation));
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return getResource(operation.getCredId())
                .flatMap(resource -> resource.mkdir(operation));
    }

    @Override
    public Mono<String> download(DownloadOperation operation) {
        return null;
    }
}
