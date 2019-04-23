package org.onedatashare.server.model.core;

import reactor.core.publisher.Flux;

public interface Tap {
  Flux<Slice> tap(long sliceSize);
  Flux<Slice> tap(Stat stat, long sliceSize);
}
