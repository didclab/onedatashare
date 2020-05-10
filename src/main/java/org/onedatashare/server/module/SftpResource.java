package org.onedatashare.server.module;

import lombok.SneakyThrows;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.module.resource.Resource;
import org.onedatashare.server.module.vfsBase.VfsResource;
import reactor.core.publisher.Mono;

public class SftpResource extends VfsResource {

    @SneakyThrows
    public SftpResource(EndpointCredential credential) {
        super(credential);
        this.fileSystemOptions = new FileSystemOptions();
        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;
        SftpFileSystemConfigBuilder.getInstance()
                .setPreferredAuthentications(fileSystemOptions,"password,keyboard-interactive");
        //Handling authentication
        if(accountCredential.getUsername() != null) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(accountCredential.getAccountId(), accountCredential.getUsername(), accountCredential.getSecret());
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, auth);
        }
        this.fileSystemManager = VFS.getManager();
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(s -> {
            try {
                SftpResource sftpResource= new SftpResource(credential);
                s.success(sftpResource);
            } catch (Exception e) {
                s.error(e);
            }
            return;
        });
    }
}
