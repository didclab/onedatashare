/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.model.core;


import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resource model that holds identifiers for a file/folder of an endpoint
 * such as file/folder path and id (for Google Drive)
 * @param <S>
 * @param <R>
 */
@Data
public abstract class Resource<S extends Session<S, R>, R extends Resource<S, R>> {

  private final String path;
  private final S session;
  private String id;
  private boolean fileResource;    // flag to identify if the current resource is a file

  protected Resource(S session, String path) {
    if (path == null)
      path = "/";
    this.path = path;
    this.session = session;
  }

  protected Resource(S session, String path, String id) {
    if (path == null)
      path = "/";
    this.path = path;
    this.session = session;
    this.id = id;
  }

  protected Resource(S session) {
    this(session, null,null);
  }

  public abstract Mono<R> select(String path);

  public final Mono<R> initialize() {
    return session.initialize().then(Mono.just((R) this));
  }

  public Mono<Stat> stat() {
    throw unsupported("stat");
  }

  public Mono<Stat> stat(String folderId) {
    throw unsupported("stat");
  }

  public Flux<String> list() {
    throw unsupported("list");
  }

  public Mono<R> mkdir() {
    throw unsupported("mkdir");
  }

  public Mono<String> download() {
    throw unsupported("download");
  }

  public Mono<R> delete() {
    throw unsupported("delete");
  }

  public abstract  Mono<Stat> getTransferStat();

  private UnsupportedOperationException unsupported(String op) {
    throw new UnsupportedOperationException(
            "The " + op + " operation is unsupported.");
  }

  public Mono<R> reselectOn(S session) {
    if (session == null)
      throw new IllegalArgumentException();
    if (session == this.session)
      return Mono.just((R)this);
    if (!session.equals(this.session))
      throw new IllegalArgumentException();
    return session.select(path,id);
  }

}
