package org.onedatashare.server.module.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

public class VfsSession extends Session<VfsSession, VfsResource> {

  FileSystemManager fileSystemManager;
  FileSystemOptions fileSystemOptions;

  public VfsSession(URI uri, Credential credential) {
    super(uri, credential);
  }

  @Override
  public Mono<VfsResource> select(String path) {
    FileObject fo = null;
    try {
      fo = fileSystemManager.resolveFile(path, fileSystemOptions);
    } catch (FileSystemException e) {
      e.printStackTrace();
    }
    return initialize().then(Mono.just(new VfsResource(this, path, fo)));
  }

  @Override
  public Mono<VfsResource> select(String path, String id) {
    FileObject fo = null;
    try {
      fo = fileSystemManager.resolveFile(path, fileSystemOptions);
      System.out.println("Mode 2 " + uri.toString());
    } catch (FileSystemException e) {
      e.printStackTrace();
    }
    return initialize().then(Mono.just(new VfsResource(this, path, fo)));
  }

  @Override
  public Mono<VfsResource> select(String path, String id, ArrayList<IdMap> idMap) {
    FileObject fo = null;
    try {
      fo = fileSystemManager.resolveFile(path, fileSystemOptions);
      System.out.println("Mode z " + uri.toString());

    } catch (FileSystemException e) {
      e.printStackTrace();
    }
    return initialize().then(Mono.just(new VfsResource(this, path, fo)));
  }

  @Override
  public Mono<VfsSession> initialize() {

    return Mono.create(s -> {
      fileSystemOptions = new FileSystemOptions();
      FtpFileSystemConfigBuilder.getInstance().setPassiveMode(fileSystemOptions, true);

      if(credential instanceof UserInfoCredential && ((UserInfoCredential) credential).getUsername() != null) {
        UserInfoCredential cred = (UserInfoCredential) credential;
        StaticUserAuthenticator auth = new StaticUserAuthenticator(uri.getHost(), cred.getUsername(), cred.getPassword());
        try {
          DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(fileSystemOptions, auth);
          fileSystemManager = VFS.getManager();
          s.success(this);
        } catch (FileSystemException e) {
          e.printStackTrace();
          s.error(new AuthenticationRequired("Invalid credential"));
        }
      }
      else {
        try {
          fileSystemManager = VFS.getManager();
          fileSystemManager.resolveFile(uri.toString());
          s.success(this);
        } catch (FileSystemException e) {
          s.error(new AuthenticationRequired("userinfo"));
        }catch (Exception e){
          e.printStackTrace();
        }
      }
    });
  }
}