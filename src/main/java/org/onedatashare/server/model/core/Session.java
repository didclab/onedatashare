package org.onedatashare.server.model.core;

import lombok.Data;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

/**
 * The model that maintains the session for a specific resource.
 * The session is maintained indirectly by retaining the enpoint URI and the corresponding credential of the resource.
 * Logically, in most cases, a new HTTP connection is made for every activity performed for a particular resource.
 * @param <S>
 * @param <R>
 */
@Data
public abstract class Session<S extends Session<S, R>, R extends Resource<S, R>> {
  private final URI uri;
  private final Credential credential;
  private boolean closed;

  /**
   * Default constructor.
   * Not used. Created to avoid an error arising due to default behaviour of Java compiler during inheritance.
   */
  protected Session(){
    this.uri = null;
    this.credential = null;
  }

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
      throw new IllegalStateException("Session is already closed.");
    }
    closed = true;
    return Mono.just((S) this);
  }
}
