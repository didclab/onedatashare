package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.useraction.UserAction;
import reactor.core.publisher.Mono;

public interface ResourceService<R extends Resource> {
  Mono<Stat> list(String cookie, UserAction userAction);
  Mono<Stat> mkdir(String cookie, UserAction userAction);
  Mono<R> delete(String cookie, UserAction userAction);
  Mono<Job> submit(String cookie, UserAction userAction);
  Mono<String> download(String cookie, UserAction userAction);
}
