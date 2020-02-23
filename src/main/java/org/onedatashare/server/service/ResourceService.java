package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.useraction.UserAction;
import reactor.core.publisher.Mono;

public interface ResourceService {
    Mono<Stat> list(String cookie, UserAction userAction);
    Mono<Boolean> mkdir(String cookie, UserAction userAction);
    Mono<Boolean> delete(String cookie, UserAction userAction);
    Mono<Job> submit(String cookie, UserAction userAction);
    Mono<String> download(String cookie, UserAction userAction);
}
