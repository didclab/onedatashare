package org.onedatashare.server.module.vfs;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.HostFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
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

  public static URI getURIWithPortNumber(URI buildItem, String portNum){
      if(StringUtils.isNumeric(portNum) && portNum.length() <= 5 && portNum.length() > 0){
          try {
              int portNumber = Integer.parseInt(portNum);
              URI historyItem = new URI(buildItem.getScheme(),
                  buildItem.getUserInfo(), buildItem.getHost(), portNumber,
                  buildItem.getPath(), buildItem.getQuery(), buildItem.getFragment());
              return historyItem;
          }catch(URISyntaxException e){
              e.printStackTrace();
              return buildItem;
          }
      }
      return buildItem;
  }

  @Override
  public Mono<VfsResource> select(String path, String portNum) {
      FileObject fo = null;

      String pathWithPort = getURIWithPortNumber(URI.create(path), portNum).toString();
      try {
          fo = fileSystemManager.resolveFile(pathWithPort, fileSystemOptions);
      } catch (FileSystemException e) {
          e.printStackTrace();
      }
      return initialize().then(Mono.just(new VfsResource(this, pathWithPort, fo)));
  }

  @Override
  public Mono<VfsResource> select(String path, String id, ArrayList<IdMap> idMap) {
    FileObject fo = null;
    try {
      fo = fileSystemManager.resolveFile(path, fileSystemOptions);
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
            SftpFileSystemConfigBuilder sfscb = SftpFileSystemConfigBuilder.getInstance();
            sfscb.setPreferredAuthentications(fileSystemOptions,"password,keyboard-interactive");
            if(getCredential() instanceof UserInfoCredential && ((UserInfoCredential) getCredential()).getUsername() != null) {
                UserInfoCredential cred = (UserInfoCredential) getCredential();
                StaticUserAuthenticator auth = new StaticUserAuthenticator(getUri().getHost(), cred.getUsername(), cred.getPassword());

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
