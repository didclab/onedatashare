package org.onedatashare.server.service;

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.ftp.FtpResource;
import org.onedatashare.server.module.Resource;
import org.onedatashare.server.service.oauth.ResourceServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class FtpService extends ResourceServiceBase {
    @Autowired
    private CredentialService credentialService;

    private static final EndpointType ENDPOINT_TYPE = EndpointType.ftp;

    @Override
    protected Mono<? extends Resource> getResource(String credId) {
        return credentialService.fetchAccountCredential(this.ENDPOINT_TYPE, credId)
                .flatMap(FtpResource::initialize)
                .subscribeOn(Schedulers.elastic());
    }

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return this.getResource(listOperation.getCredId())
                .flatMap(resource -> resource.list(listOperation));
    }

    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return this.getResource(source.getCredId())
                .flatMap(resource -> resource.listAllRecursively(source));
    }
}
