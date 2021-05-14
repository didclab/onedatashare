package org.onedatashare.server.module;

import lombok.SneakyThrows;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.exceptions.FileNotFoundException;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public class SftpResource extends VfsResource {
    private static final String CONTENT_DISPOSITION_HEADER = "attachment; filename=\"%s\"";

    @SneakyThrows
    public SftpResource(EndpointCredential credential) {
        super(credential);
        this.fileSystemOptions = new FileSystemOptions();
        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;
        SftpFileSystemConfigBuilder.getInstance()
                .setPreferredAuthentications(fileSystemOptions,"password,keyboard-interactive");
        //Handling authentication
        if(accountCredential.getUsername() != null) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(accountCredential.getUri(), accountCredential.getUsername(), accountCredential.getSecret());
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, auth);
        }
        this.fileSystemManager = VFS.getManager();
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(sink -> {
            try {
                SftpResource sftpResource= new SftpResource(credential);
                sink.success(sftpResource);
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono download(DownloadOperation operation){
        return Mono.create(s -> {
            try {
                FileObject fileObject = this.resolveFile(this.baseUri + operation.getId() + operation.fileToDownload);
                if(!fileObject.exists()){
                    s.error(new FileNotFoundException());
                    return;
                }
                InputStream inputStream = fileObject.getContent().getInputStream();
                String filename = operation.fileToDownload;
                InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
                HttpHeaders httpHeaders = new HttpHeaders();

                httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_HEADER, filename));
                ResponseEntity entity = ResponseEntity.ok()
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(inputStreamResource);
                s.success(entity);
            } catch (FileSystemException e) {
                s.error(e);
            }
        });
    }
}