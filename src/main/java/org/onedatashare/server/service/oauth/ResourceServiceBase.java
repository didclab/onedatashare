package org.onedatashare.server.service.oauth;

import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.resource.Resource;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class ResourceServiceBase {

    protected abstract Mono<? extends Resource> getResource(TransferJobRequest.Source source);

    public abstract Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source);
}
