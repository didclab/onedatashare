package org.onedatashare.server.module.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.IdMap;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class S3Session extends Session<S3Session, S3Resource> {

    private AmazonS3 s3Client;

    public S3Session(URI uri, Credential credential) {
        super(uri, credential);
    }

    @Override
    public Mono<S3Resource> select(String path) {
        System.out.println(path);
        return null;
    }

    @Override
    public Mono<S3Resource> select(String path, String id, ArrayList<IdMap> idMap) {
        return null;
    }

    @Override
    public Mono<S3Session> initialize() {
        return Mono.create(s -> {
            if(getCredential() instanceof UserInfoCredential && ((UserInfoCredential) getCredential()).getUsername() != null) {
                UserInfoCredential cred = (UserInfoCredential) getCredential();
                String accessKey =  cred.getUsername();
                String secretKey = cred.getPassword();

                AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                s3Client = new AmazonS3Client(credentials);

            }

        });
        }

    }

