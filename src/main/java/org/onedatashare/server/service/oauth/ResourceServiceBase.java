package org.onedatashare.server.service.oauth;

import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.resource.Resource;
import reactor.core.publisher.Mono;

public abstract class ResourceServiceBase {

    protected abstract Mono<? extends Resource> getResource(TransferJobRequest.Source source);

    public abstract Mono<TransferJobRequest.Source> listAllRecursively(TransferJobRequest.Source source);
}
