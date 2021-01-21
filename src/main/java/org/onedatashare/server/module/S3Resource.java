package org.onedatashare.server.module;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class S3Resource extends Resource{

    private final AccountEndpointCredential accountEndpointCredential;
    private final String bucketName;
    private final String currentBucketRegion;

    public S3Resource(EndpointCredential endpointCredential){
        this.accountEndpointCredential = (AccountEndpointCredential) endpointCredential;
        String[] regionAndBucket = this.accountEndpointCredential.getUri().split(":::");
        this.currentBucketRegion = regionAndBucket[0];
        this.bucketName = regionAndBucket[1];
    }

    public SdkAsyncHttpClient defaultHttpClientToUse(){
        return NettyNioAsyncHttpClient.builder()
                .maxConcurrency(16)
                .writeTimeout(Duration.ZERO)
                .build();
    }
    public S3Configuration defaultClientConfig(){
        return S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();
    }

    private ListObjectsV2Request listObjectsV2Request(String bucketName, String path){
        return ListObjectsV2Request.builder().bucket(bucketName).prefix(path).build();
    }

    private DeleteObjectRequest buildDeleteObjectRequest(String bucketName, String key){
        return DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
    }

    private S3AsyncClient clientBuilder(){
        StaticCredentialsProvider provider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accountEndpointCredential.getAccountId(), accountEndpointCredential.getSecret()));
         return S3AsyncClient.builder()
                .serviceConfiguration(defaultClientConfig())
                .httpClient(defaultHttpClientToUse())
                .region(Region.of(this.currentBucketRegion))
                .credentialsProvider(provider)
                .build();
    }

    /**
     * Might need to handle 3 cases here not sure yet but they would be:
     * 1. Deleting a file only this is known if you are given a key
     * 2. Deleting a "folder/directory" if it ends with a SUFFIX "/" then you are deleting a grouping and you need to list all the contents of the group and then delete each one. After deleting the folder finally
     * 3. Deleting a Bucket: Delete all of the contents of A bucket and then finally deleting the bucket.
     * @param operation
     * @return
     */
    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        if(operation.getPath().endsWith("/")){
            this.list(new ListOperation(operation.getCredId(), operation.getPath(), operation.getId()))
                    .map(stat -> Mono.fromCompletionStage(
                    this.clientBuilder().deleteObject(DeleteObjectRequest.builder().bucket(this.bucketName).key(stat.getId()).build())
                    ));
        }
        return Mono.create(sinkResponse -> {
             Mono.fromCompletionStage(clientBuilder().deleteObject(buildDeleteObjectRequest(this.bucketName, operation.getPath())));
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.fromCompletionStage(
            clientBuilder().listObjectsV2(listObjectsV2Request(this.bucketName, operation.getPath()))
        ).map(listObjectsV2Response -> {
           Stat parent = convertS3ObjectToStat(null, true);
           List<Stat> children = parent.getFilesList();
           listObjectsV2Response.contents().forEach(metaData -> {
               children.add(convertS3ObjectToStat(metaData, false));
           });
           parent.setFiles(children);
           return parent;
        });
    }

    /**
     * There is no concept of directories in S3 as it is a flat file system!
     * @param operation
     * @return
     */
    @Override
    public Mono<Void> mkdir(MkdirOperation operation) { return null; }

    /**
     * Currently this will get relocated to the Transfer-Service.
     * @param operation
     * @return
     */
    @Override
    public Mono download(DownloadOperation operation) {
        return null;
    }

    /**
     * This is getting moved to the Transfer-Scheduler Service
     * @param source
     * @return
     */
    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) { return null; }

    public static Mono<? extends Resource> initialize(EndpointCredential accountEndpointCredential) {
        return Mono.create(resourceSink -> {
            S3Resource s3Resource = new S3Resource(accountEndpointCredential);
            resourceSink.success(s3Resource);
        });
    }

    /**
     * A Parent is simply the stat that holds an ArrayList of Children with no ordering to it.
     * S3 IS A FLAT FILE SYSTEM!
     * To Group you
     * @param s3ObjectOfMetaData
     * @param isParent
     * @return
     */
    public Stat convertS3ObjectToStat(S3Object s3ObjectOfMetaData, boolean isParent){
        if(isParent){
            Stat parent = new Stat();
            parent.setId("Container Stat object");
            parent.setFiles(new ArrayList<>());
            parent.setName("Parent Container");
            return parent;
        }else{
            Stat child = new Stat();
            child.setId(s3ObjectOfMetaData.key());
            child.setName(s3ObjectOfMetaData.key());
            child.setDir(false);
            child.setFile(true);
            child.setTime(s3ObjectOfMetaData.lastModified().getEpochSecond());
            child.setSize(s3ObjectOfMetaData.size());
            child.setPermissions(s3ObjectOfMetaData.eTag());
            return child;
        }
    }
}
