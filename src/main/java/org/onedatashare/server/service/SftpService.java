package org.onedatashare.server.service;

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.SftpResource;
import org.onedatashare.server.module.Resource;
import org.onedatashare.server.service.oauth.ResourceServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class SftpService extends ResourceServiceBase {
    @Autowired
    private CredentialService credentialService;

    private static final EndpointType ENDPOINT_TYPE = EndpointType.sftp;

    @Override
    protected Mono<? extends Resource> getResource(String credId) {
        return credentialService.fetchAccountCredential(this.ENDPOINT_TYPE, credId)
                .flatMap(SftpResource::initialize)
                .subscribeOn(Schedulers.elastic());
    }

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return getResource(listOperation.getCredId())
                .flatMap(resource -> resource.list(listOperation));
    }

    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return getResource(source.getCredId())
                .flatMap(resource -> resource.listAllRecursively(source));
    }
}
