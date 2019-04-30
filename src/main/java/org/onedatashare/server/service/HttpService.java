package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.http.HttpResource;
import reactor.core.publisher.Mono;

public class HttpService implements ResourceService<HttpResource> {
    @Override
    public Mono<Stat> list(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    /* Not allowed */
    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    /* Not allowed */
    public Mono<HttpResource> delete(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    /* Returns the download link of the file to be downloaded */
    /* TODO: handle at the front-end */
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }
}
