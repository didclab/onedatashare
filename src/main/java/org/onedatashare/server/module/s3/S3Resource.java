package org.onedatashare.server.module.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.module.ftp.FtpResource;
import org.onedatashare.server.module.s3.S3Session;
import org.onedatashare.server.service.ODSLoggerService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Resource class that provides services for FTP, SFTP and SSH protocols
 */
public class S3Resource extends Resource<S3Session, S3Resource> {

    public AmazonS3Client s3Client;

    protected S3Resource(S3Session session, String path) {
        super(session, path);
    }

    public Mono<S3Resource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try{
                String[] currpath = getPath().split("/");
                String folderName = "";
                for(int i = 2; i < currpath.length; i++){
                    folderName += "/" + currpath[i];
                }
                folderName = folderName.replaceFirst("/", "");
                folderName+="/";
                String bucketName = currpath[1];
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(0);

                InputStream content = new ByteArrayInputStream(new byte[0]);
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName, content, metadata);
                s3Client.putObject(putObjectRequest);
            } catch(Exception e){
                e.printStackTrace();
            }
        });

    }

    public Mono<S3Resource> delete() {
        return initialize().map(resource -> {
            try{
                String path = getPath();
                String[] currpath = path.split("/");
                String objPath = currpath[currpath.length - 1];
                ArrayList<String> paths = new ArrayList<>();
                String bucketName = currpath[1];
                List<S3ObjectSummary> bucketContents = s3Client.listObjects(bucketName).getObjectSummaries();
                for(S3ObjectSummary sum : bucketContents){
                    String key = sum.getKey();
                    if (key.contains(objPath)){
                        paths.add(key);
                    }
                }
                for (String p : paths){
                    s3Client.deleteObject(bucketName, p);
                }
            } catch(AmazonClientException ace) {
                ace.printStackTrace();
            }
            return this;
        });
    }


    @Override
    public Mono<S3Resource> select(String path) {
        //return getSession().select(path);
        return Mono.just(new S3Resource(null, path));
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    private Stat onStat() {
        AmazonS3Client S3Client = s3Client;
        String path = getPath();
        if (getPath().equals("amazons3/")){
            List<Bucket> bucketList = S3Client.listBuckets();
            Stat bucketStat = buildRootStat(bucketList);
            return bucketStat;
        }
        String bucketName= path.split("/")[1]; //Get Bucket name
        String subpath = path.replace("amazons3/" + bucketName + "/", "");
        Boolean isBucket = path.equals("amazons3/" + bucketName);
        if(isBucket){
            List<S3ObjectSummary> bucketContents = S3Client.listObjects(bucketName).getObjectSummaries();
            Stat bucketContentStat = buildStat(bucketContents, subpath, isBucket);
            return bucketContentStat;
        }
        List<S3ObjectSummary> contents = S3Client.listObjectsV2(bucketName, subpath).getObjectSummaries();
        Stat contentStat = buildStat(contents, subpath, isBucket);
        return contentStat;
    }

    //Builds the stat within a bucket
    public Stat buildStat(List<S3ObjectSummary> objectList, String subpath, boolean isBucket){
        if(!isBucket) objectList.remove(0); //Remove the first element if not a bucket, as it's always a duplicate of itself
        Stat stat = new Stat();
        ArrayList<Stat> contents =  new ArrayList<>();
        boolean isDir;
        LinkedHashSet<String> paths = new LinkedHashSet();

        for(S3ObjectSummary obj : objectList){
            Stat fileStat = new Stat();
            String name = obj.getKey().replace(subpath + "/", ""); //Remove the common subpath from each object
            isDir = name.contains("/");
            name = name.split("/")[0];  //Get the first directory/file from the path
            if(!paths.contains(name)) {
                paths.add(name);
                if(isDir){
                    fileStat.setFile(false);
                    fileStat.setDir(true);
                }
                else{
                    fileStat.setFile(true);
                    fileStat.setDir(false);
                }
                fileStat.setName(name);
                fileStat.setTime(obj.getLastModified().getTime());
                contents.add(fileStat);
            }

        }
        stat.setFiles(new Stat[contents.size()]);
        stat.setFiles(contents.toArray((stat.getFiles())));
        return stat;
    }
    //builds the stat that holds the buckets
    public Stat buildRootStat(List<Bucket> bucketList){
        Stat stat = new Stat();                         //All buckets under this stat
        ArrayList<Stat> contents = new ArrayList<>();
        for(Bucket bucket : bucketList){
            Stat bucketStat = new Stat();               //Individual bucket under this stat
            bucketStat.setDir(true);
            bucketStat.setFile(false);
            bucketStat.setName(bucket.getName());
            bucketStat.setTime(bucket.getCreationDate().getTime());
            bucketStat.setPermissions(bucket.getOwner().getDisplayName());
            contents.add(bucketStat);

        }
        stat.setFiles(new Stat[contents.size()]);
        stat.setFiles(contents.toArray(stat.getFiles()));
        return stat;
    }


    public VfsTap tap() {
        return null;
    }

    public VfsDrain sink() {
        return new VfsDrain().start();
    }

    public VfsDrain sink(Stat stat) {
        return new VfsDrain().start(getPath() + stat.getName());
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }

    public class VfsTap implements Tap {
        FileContent fileContent;
        long size;

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
            String downloadPath = getPath();
            if (!isFileResource())
                downloadPath += stat.getName();
            size = stat.getSize();
            return tap(sliceSize);
        }

        public Flux<Slice> tap(long sliceSize) {
            int sliceSizeInt = Math.toIntExact(sliceSize);
            InputStream inputStream = null;
            try {
                inputStream = fileContent.getInputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            InputStream finalInputStream = inputStream;
            return Flux.generate(
                    () -> 0L,
                    (state, sink) -> {
                        if (state + sliceSizeInt < size) {
                            byte[] b = new byte[sliceSizeInt];
                            try {
                                // Fix for buggy PDF files - Else the PDF files are corrupted
                                for(int offset = 0; offset < sliceSizeInt; offset+=1)
                                    finalInputStream.read(b, offset, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                        } else {
                            int remaining = Math.toIntExact(size - state);
                            byte[] b = new byte[remaining];
                            try {
                                // Fix for buggy PDF files - Else the PDF files are corrupted
                                for(int offset = 0; offset < remaining; offset+=1)
                                    finalInputStream.read(b, offset, 1);
                                finalInputStream.close();
                                sink.next(new Slice(b));
                                sink.complete();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return state + sliceSizeInt;
                    });
        }
    }

    class VfsDrain implements Drain {
        OutputStream outputStream;

        @Override
        public VfsDrain start() {
            return this;
        }

        @Override
        public VfsDrain start(String drainPath) {
            return null;
        }

        @Override
        public void drain(Slice slice) {
            try {
                outputStream.write(slice.asBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void finish() {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Mono<String> generateDownloadLink() {
        String downloadLink = "";
        try{
            String path = getPath();
            String[] currpath = path.split("/");
            String fileName = "";
            for(int i = 2; i < currpath.length; i++){
                fileName += "/" + currpath[i];
            }
            fileName = fileName.replaceFirst("/", "");
            String bucketName = currpath[1];
//                downloadLink = getSession().s3Client.getResourceUrl(bucketName, "report/" + fileName);
//                fileName = URLEncoder.encode(fileName, "UTF-8");
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, fileName);
            ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
            overrides.setContentDisposition("attachment; filename=" + fileName);
            req.setResponseHeaders(overrides);
            //System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
            //URL url = getSession().s3Client.generatePresignedUrl(req);
            URL url = s3Client.generatePresignedUrl(req);
            downloadLink = url.toString();
//                downloadLink = URLEncoder.encode(url.toString(), "UTF-8");

        } catch(AmazonClientException ace) {
            ace.printStackTrace();

        }
        return Mono.just(downloadLink);
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential) {
        return Mono.create(s -> {
            AccountEndpointCredential cred = (AccountEndpointCredential) credential;
            String accessKey =  cred.getUsername();
            String secretKey = cred.getSecret();

            if(accessKey != null && secretKey != null) {
                AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                s3Client = new AmazonS3Client(credentials);
                s.success();
            }
        });
    }
}