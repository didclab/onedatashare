package org.onedatashare.server.module;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class S3Resource extends Resource{

    AccountEndpointCredential credential;
    S3AsyncClient s3AsyncClient;
    String[] regionAndBucket;

    public S3Resource(EndpointCredential credential){
        this.credential = (AccountEndpointCredential) credential;
        regionAndBucket = this.credential.getUri().split(":::");
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(s -> {
            try {
                AccountEndpointCredential accountEndpointCredential = (AccountEndpointCredential) credential;
                String[] regionAndBucketName = accountEndpointCredential.getUri().split(":::");
                S3Resource s3Resource = new S3Resource(accountEndpointCredential);
                s3Resource.credential = accountEndpointCredential;
                s3Resource.s3AsyncClient = S3AsyncClient.builder()
                        .credentialsProvider(new AwsCredentialsProvider() {
                            @Override
                            public AwsCredentials resolveCredentials() {
                                return AwsBasicCredentials.create(accountEndpointCredential.getUsername(), accountEndpointCredential.getSecret());
                            }
                        })
                        .region(Region.of(regionAndBucketName[0]))
                        .build();
                s3Resource.regionAndBucket = accountEndpointCredential.getAccountId().split(":::");
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
                    .key(operation.getId())
                    .bucket(this.regionAndBucket[1])
                    .build();
            CompletableFuture<DeleteObjectResponse> response = this.s3AsyncClient.deleteObject(deleteObjectRequest);
            monoSink.success();
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        Stat parent = new Stat();
        parent.setTime(System.nanoTime());
        return Mono.create(monoSink -> {
            ListObjectsV2Request v2Request = ListObjectsV2Request.builder().bucket(this.regionAndBucket[1]).prefix(operation.getPath()).build();
            Mono.fromFuture(this.s3AsyncClient.listObjectsV2(v2Request)).map(
                    listObjectsV2Response -> {
                        parent.setFilesList(s3ObjectListToStatList(listObjectsV2Response.contents()));
                        return parent;
                    });
            monoSink.success(parent);
        });
    }

    private List<Stat> s3ObjectListToStatList(List<S3Object> s3ObjectList){
        return s3ObjectList.parallelStream().map(this::s3ObjectToStat).collect(Collectors.toList());
    }

    private Stat s3ObjectToStat(S3Object s3Object){
        Stat stat = new Stat();
        stat.setName(s3Object.key());
        stat.setFile(true);
        stat.setSize(s3Object.size());
        stat.setId(s3Object.eTag());
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

    /**
     * Listing is now done on the Scheduler and this is a FlatFile system so probably even less of a reason this is needed.
     * @param source
     * @return
     */
    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return null;
    }

}
