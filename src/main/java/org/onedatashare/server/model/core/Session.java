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

  /**
   * Default constructor.
   * Not used. Created to avoid an error arising due to default behaviour of Java compiler during inheritance.
   */
  protected Session(){
    this.uri = null;
    this.credential = null;
  }

  /**
   * Parameterised contrustor that calls another parameterized constructor with a null value for credential
   * @param uri - Endpoint URI of the resource
   */
  protected Session(URI uri) {
    this(uri, null);
  }

  /**
   * Parameterized constructor that sets the enpoint URI of the resource and the credential in the current session object.
   * @param uri - Endpoint URI of the resource
   * @param credential - Credential object of the resource (could be an OAuthCredential containing a token
   *                   or a UserActionCredential containing raw username and password)
   */
  protected Session(URI uri, Credential credential) {
    this.uri = uri;
    this.credential = credential;
  }

  /**
   * This method creates the resource object corresponding to the session endpoint type.
   * All child classes must override this method and generate a resource object in return.
   *
   * @param path - relative path of the file/folder in the resource (different from URI which also consists the
   *             protocol type appended to the relative path)
   * @return a resource instance corresponding to endpoint type
   */
  public Mono<R> select(String path){return select(path, null,null);}

  /**
   * Overloaded version of the select method that includes the file/folder id.
   * GoogleDrive APIs assign a unique id to every file/folder, which is used to generate the corresponding resource instance.
   *
   * @param path - relative path of the file/folder in the resource
   * @param id - Unique id of file/folder
   * @return a resource instance corresponding to endpoint type
   */
  public Mono<R> select(String path, String id){return select(path, id,null);}

  /**
   * Overloaded version of the select method that includes the idMap map for a GoogleDrive type resource.
   * @param path - relative path of the file/folder in the resource
   * @param id - Unique id of file/folder
   * @param idMap
   * @return a resource instance corresponding to endpoint type
   */
  public abstract Mono<R> select(String path, String id, ArrayList<IdMap> idMap);

  /**
   * Method to initiate an authenticated HTTP session with the endpoint resource.
   * Method must be overridden in child classes to implement resource specific logic to generate an HTTP session.
   * @return HTTP session object specific to the resource type
   */
  public abstract Mono<S> initialize();

}
