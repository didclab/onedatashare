package org.onedatashare.server.module.http;

import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

public class HttpSession extends Session<HttpSession, HttpResource> {
    protected HttpSession(URI uri) {
        super(uri);
    }

    @Override
    public Mono<HttpResource> select(String path, String id, ArrayList<IdMap> idMap) {
        return null;
    }

    @Override
    public Mono<HttpSession> initialize() {
        return null;
    }
}
