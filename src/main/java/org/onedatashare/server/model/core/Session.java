package org.onedatashare.server.model.core;

import lombok.Data;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Data
public abstract class Session<S extends Session<S, R>, R extends Resource<S, R>> {
  public final URI uri;
  public final Credential credential;
  public boolean closed;

  protected Session(URI uri) {
    this(uri, null);
  }

  protected Session(URI uri, Credential credential) {
    this.uri = uri;
    this.credential = credential;
  }

  public Mono<R> select(String path){return select(path, null,null);}

  public Mono<R> select(String path, String id){return select(path, id,null);}

  public abstract Mono<R> select(String path, String id, ArrayList<IdMap> idMap);

  public abstract Mono<S> initialize();

  protected void finalize() {
    close().subscribe();
  }

  public final synchronized Mono<S> close() {
    return close(null);
  }

  public final synchronized Mono<S> close(Throwable reason) {
    if (reason == null) {
      reason = new IllegalStateException("Session is closed.");
    }
    closed = true;
    return Mono.just((S) this);
  }

  public final synchronized void closeWhen(Mono mono) {
    mono.doOnSuccess(s -> close()).doOnError(t -> close((Throwable) t)).subscribe();
  }
}
