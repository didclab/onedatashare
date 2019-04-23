package org.onedatashare.server.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.vfs.VfsResource;
import org.onedatashare.server.module.vfs.VfsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class VfsService implements ResourceService<VfsResource> {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    public Mono<VfsResource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        userAction = fixSCPUri(userAction);
        final String path = pathFromUri(userAction.uri);
        final UserAction finalUserAction = userAction;
        return userService.getLoggedInUser(cookie)
                .map(user -> new UserInfoCredential(finalUserAction.credential))
                .map(credential -> new VfsSession(URI.create(finalUserAction.uri), credential))
                .flatMap(VfsSession::initialize)
                .flatMap(vfsSession -> vfsSession.select(path));
    }

    public Mono<VfsResource> getResourceWithUserActionUri(String cookie, String userActionString) {
        List<String> path = new ArrayList<>();
        return userService.getLoggedInUser(cookie)
                .map(user -> {
                    byte[] encryptedString = Base64.getDecoder().decode(userActionString);
                    byte[] privateKeyString = Base64.getDecoder().decode(user.getPrivateKey());
                    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyString);
                    KeyFactory fact = null;
                    try {
                        fact = KeyFactory.getInstance("RSA");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    PrivateKey privateKey = null;
                    try {
                        privateKey = fact.generatePrivate(spec);
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                    Cipher decrypt = null;
                    try {
                        decrypt = Cipher.getInstance("RSA");
                        decrypt.init(Cipher.DECRYPT_MODE, privateKey);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                    String decryptedMessage = null;
                    try {
                        decryptedMessage = new String(decrypt.doFinal(encryptedString), StandardCharsets.UTF_8);
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(decryptedMessage);
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserActionResource userActionResource = null;
                    try {
                        userActionResource = objectMapper.readValue(decryptedMessage, UserActionResource.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    UserInfoCredential userInfoCredential = new UserInfoCredential(userActionResource.credential);
                    VfsSession vfsSession = new VfsSession(URI.create(userActionResource.uri), userInfoCredential);
                    path.add(pathFromUri(userActionResource.uri));
                    return vfsSession;
                }).flatMap(VfsSession::initialize)
                .flatMap(vfsSession -> vfsSession.select(path.get(0)));
    }

    public Mono<VfsResource> getResourceWithUserActionResource(String cookie, UserActionResource userActionResource) {
        userActionResource = fixSCPUri(userActionResource);
        System.out.println(userActionResource.toString());
        final String path = pathFromUri(userActionResource.uri);
        final UserActionResource finalUserActionResource = userActionResource;
        return userService.getLoggedInUser(cookie)
                .map(user -> new UserInfoCredential(finalUserActionResource.credential))
                .map(credential -> new VfsSession(URI.create(finalUserActionResource.uri), credential))
                .flatMap(VfsSession::initialize)
                .flatMap(vfsSession -> vfsSession.select(path));
    }

    public UserAction fixSCPUri(UserAction userAction){
        if(userAction.type.equals("scp://")){
            userAction.type = "sftp://";
            userAction.uri = "sftp://" + userAction.uri.substring(6);
        }
        return userAction;
    }

    public UserActionResource fixSCPUri(UserActionResource userAction){
        if(userAction.type.equals("scp://")){
            userAction.type = "sftp://";
            userAction.uri = "sftp://" + userAction.uri.substring(6);
        }
        return userAction;
    }

    public String pathFromUri(String uri) {
        String path = "";
        if (uri.contains("dropbox://")) {
            path = uri.split("dropbox://")[1];
        } else path = uri;
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(VfsResource::stat);
    }

    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(VfsResource::mkdir)
                .flatMap(VfsResource::stat);
    }

    public Mono<VfsResource> delete(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction)
                .flatMap(VfsResource::delete);
    }

    public Mono<Job> submit(String cookie, UserAction userAction) {
        return userService.getLoggedInUser(cookie)
                .map(user -> {
                    Job job = new Job(userAction.src, userAction.dest);
                    job.setStatus(JobStatus.scheduled);
                    job = user.saveJob(job);
                    userService.saveUser(user).subscribe();
                    return job;
                })
                .flatMap(jobService::saveJob)
                .doOnSuccess(job -> processTransferFromJob(job, cookie))
                .subscribeOn(Schedulers.elastic());
    }

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }

    public void processTransferFromJob(Job job, String cookie) {
        Transfer<Resource, Resource> transfer = new Transfer<>();
        getResourceWithUserActionResource(cookie, job.src)
                .map(transfer::setSource)
                .flatMap(t -> getResourceWithUserActionResource(cookie, job.dest))
                .map(transfer::setDestination)
                .flux()
                .flatMap(transfer1 -> transfer1.start(1L << 20))
                .doOnSubscribe(s -> job.setStatus(JobStatus.processing))
                .doFinally(s -> {
                    job.setStatus(JobStatus.complete);
                    jobService.saveJob(job).subscribe();
                })
                .map(job::updateJobWithTransferInfo)
                .flatMap(jobService::saveJob)
                .subscribe();
    }

    public Mono<String> getDownloadURL(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(VfsResource::getDownloadURL);
    }

    public Mono<ResponseEntity> getSftpDownloadStream(String cookie, UserActionResource userActionResource) {
        return getResourceWithUserActionResource(cookie, userActionResource).flatMap(VfsResource::getSftpObject);
    }
}
