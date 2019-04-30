package org.onedatashare.server.module.http;

import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import reactor.core.publisher.Mono;

public class HttpResource extends Resource<HttpSession, HttpResource> {
    protected HttpResource(HttpSession session) {
        super(session);
    }

    @Override
    public Mono<HttpResource> select(String path) {
        return null;
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }
}
