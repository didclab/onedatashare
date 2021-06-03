package org.onedatashare.server.module;

import org.apache.log4j.Logger;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class S3Resource extends Resource{

    private final static Logger logger = Logger.getLogger(S3Resource.class);

    private S3AsyncClient s3AsyncClient;
    String[] regionAndBucket;

    public S3Resource(EndpointCredential credential){
        super(credential);
        this.regionAndBucket = ((AccountEndpointCredential) credential).getUri().split(":::");
        this.s3AsyncClient = this.constructClient(regionAndBucket[0], (AccountEndpointCredential) credential);
    }

    private S3AsyncClient constructClient(String region, AccountEndpointCredential credential){
        return S3AsyncClient.builder()
                .serviceConfiguration(S3Configuration.builder().checksumValidationEnabled(false).build())
                .asyncConfiguration(b -> b.advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR, Executors.newFixedThreadPool(50)))
                .credentialsProvider(() -> AwsBasicCredentials.create(credential.getUsername(), credential.getSecret()))
                .region(Region.of(region))
                .build();
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(s -> {
            try {
                AccountEndpointCredential accountEndpointCredential = (AccountEndpointCredential) credential;
                S3Resource s3Resource = new S3Resource(accountEndpointCredential);
                s.success(s3Resource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(monoSink -> {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .key(operation.getToDelete())
                    .bucket(this.regionAndBucket[1])
                    .build();
            this.s3AsyncClient.deleteObject(deleteObjectRequest);
            monoSink.success();
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.fromFuture(this.s3AsyncClient.listObjectsV2(ListObjectsV2Request.builder().bucket(this.regionAndBucket[1]).build()))
                .map(listObjectsV2Response -> {
                    Stat parent = new Stat();
                    parent.setFiles(s3ObjectListToStatList(listObjectsV2Response.contents()));
                    logger.info(parent.getFilesList());
                    return parent;
                });
    }

    private List<Stat> s3ObjectListToStatList(List<S3Object> s3ObjectList){
        ArrayList<Stat> files = new ArrayList<>();
        for(S3Object s3Object : s3ObjectList){
            files.add(s3ObjectToStat(s3Object));
        }
        return files;
    }

    private Stat s3ObjectToStat(S3Object s3Object){
        Stat stat = new Stat();
        stat.setName(s3Object.key());
        stat.setFile(true);
        stat.setSize(s3Object.size());
        stat.setId(s3Object.key());
        stat.setTime(s3Object.lastModified().getNano());
        return stat;
    }

    /**
     * THIS IS NOT NEEDED
     * This will not be supported as AWS S3 is a flat file system as in the bucket contains only keys(files).
     * https://stackoverflow.com/questions/1939743/amazon-s3-boto-how-to-create-a-folder
     * The only way to "create a directory" would be to add the prefix onto the key/file you wish to upload
     * @param operation
     * @return
     */
    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return null;
    }

    /**
     * THIS IS NOT NEEDED
     * Download functionality will be deleted soon
     * @param operation
     * @return
     */
    @Override
    public Mono download(DownloadOperation operation) {
        return null;
    }
}
