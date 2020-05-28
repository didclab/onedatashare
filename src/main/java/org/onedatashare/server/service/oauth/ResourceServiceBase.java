package org.onedatashare.server.service.oauth;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.Resource;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class ResourceServiceBase {

    protected abstract Mono<? extends Resource> getResource(String credId);

    public abstract Mono<Stat> list(ListOperation listOperation);

    public abstract Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source);
}
