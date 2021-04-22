package org.onedatashare.server.module.ftp;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.module.Resource;
import org.onedatashare.server.module.VfsResource;
import reactor.core.publisher.Mono;

public class FtpResource extends VfsResource {

    public FtpResource(EndpointCredential credential) throws Exception{
        super(credential);
        this.fileSystemOptions = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(this.fileSystemOptions, true);

        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;

        //Handling authentication
        if(accountCredential.getUsername() != null && accountCredential.getSecret() != null) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(accountCredential.getUri(), accountCredential.getUsername(), accountCredential.getSecret());
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, auth);
        }

        this.fileSystemManager = VFS.getManager();
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(s -> {
            try {
                FtpResource ftpResource = new FtpResource(credential);
                s.success(ftpResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }
}
