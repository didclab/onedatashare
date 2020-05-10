package org.onedatashare.server.service;

import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.SftpResource;
import org.onedatashare.server.module.resource.Resource;
import org.onedatashare.server.service.oauth.ResourceServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SftpService extends ResourceServiceBase {
    @Autowired
    private CredentialService credentialService;

    @Override
    protected Mono<? extends Resource> getResource(TransferJobRequest.Source source) {
        return credentialService.fetchAccountCredential(source.getType(), source.getCredId())
                .flatMap(SftpResource::initialize);
    }

    @Override
    public Mono<TransferJobRequest.Source> listAllRecursively(TransferJobRequest.Source source) {
        return getResource(source)
                .flatMap(resource -> resource.listAllRecursively(source));
    }
}
