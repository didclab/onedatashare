package org.onedatashare.server.module;

import lombok.SneakyThrows;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.exceptionHandler.error.ODSException;
import org.onedatashare.server.model.filesystem.exceptions.FileNotFoundException;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

public class SftpResource extends VfsResource {
    private static final String CONTENT_DISPOSITION_HEADER = "attachment; filename=\"%s\"";

    /**
     * This needs to use a pem file and or basic auth like in FTP(username, password)
     * @param credential
     */
    @SneakyThrows
    public SftpResource(EndpointCredential credential) {
        super(credential);
        SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        this.fileSystemOptions = new FileSystemOptions();
        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;
        if(accountCredential.getSecret().contains("-----BEGIN RSA PRIVATE KEY-----")){
            builder.setStrictHostKeyChecking(this.fileSystemOptions, "no");
            //builder.setUserDirIsRoot(this.fileSystemOptions, false);
            builder.setIdentityInfo(this.fileSystemOptions,pubPriKey(accountCredential));
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, justUserName(accountCredential));
        }else{
            builder.setPreferredAuthentications(fileSystemOptions,"password,keyboard-interactive");
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, basicAuth(accountCredential));
        }
        this.fileSystemManager = VFS.getManager();
    }

    public StaticUserAuthenticator basicAuth(AccountEndpointCredential accountCredential){
        return new StaticUserAuthenticator(accountCredential.getUri(), accountCredential.getUsername(), accountCredential.getSecret());
    }

    @SneakyThrows
    public IdentityInfo pubPriKey(AccountEndpointCredential credential){
        if(this.credential.getAccountId().length() < 3){
            this.credential.setAccountId(this.credential.getAccountId() + "o");
        }
        File tempFile = File.createTempFile(this.credential.getAccountId(), ".pem");
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))){
            writer.write(credential.getSecret());
        }
        return new IdentityInfo(tempFile);
    }

    public StaticUserAuthenticator justUserName(AccountEndpointCredential accountEndpointCredential){
        return new StaticUserAuthenticator(accountEndpointCredential.getUri(), accountEndpointCredential.getUsername(), null);
    }

    public static Resource initialize(EndpointCredential credential){
        return new SftpResource(credential);
    }

//    @Override
//    public ResponseEntity download(DownloadOperation operation){
//        try {
//            FileObject fileObject = this.resolveFile(this.baseUri + operation.getId() + operation.fileToDownload);
//            if(!fileObject.exists()){
//                throw new FileNotFoundException();
//            }
//            InputStream inputStream = fileObject.getContent().getInputStream();
//            String filename = operation.fileToDownload;
//            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
//            HttpHeaders httpHeaders = new HttpHeaders();
//
//            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_HEADER, filename));
//            ResponseEntity entity = ResponseEntity.ok()
//                    .headers(httpHeaders)
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .body(inputStreamResource);
//            return entity;
//        } catch (FileSystemException | FileNotFoundException e) {
//            throw new ODSException(e.getMessage(),e.getClass().getName());
//        }
//    }
}
