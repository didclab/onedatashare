package org.onedatashare.server.module.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.module.s3.S3Session;
import org.onedatashare.server.service.ODSLoggerService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Resource class that provides services for FTP, SFTP and SSH protocols
 */
public class S3Resource extends Resource<S3Session, S3Resource> {

    protected S3Resource(S3Session session, String path) {
        super(session, path);
    }

    public Mono<S3Resource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try{
                String[] currpath = getPath().split("/");
                String folderName = "";
                for(int i = 2; i < currpath.length; i++){
                    folderName+=currpath[i];
                }
                folderName+="/";
                String bucketName = currpath[1];
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(0);

                InputStream content = new ByteArrayInputStream(new byte[0]);
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName, content, metadata);
                getSession().s3Client.putObject(putObjectRequest);
            } catch(Exception e){
                e.printStackTrace();
            }
        });

    }

    public Mono<S3Resource> delete() {
        return initialize().map(resource -> {
            try{
                String path = getPath();
                boolean isDir = path.substring(path.length() - 1).equals("/");
                String[] currpath = path.split("/");
                String objPath = "";
                for(int i = 2; i < currpath.length; i++){
                    objPath+=currpath[i];
                }
                String bucketName = currpath[1];
               List<S3ObjectSummary> bucketContents = getSession().s3Client.listObjects(bucketName).getObjectSummaries();
                if(isDir){
                    objPath+="/";

                }
                getSession().s3Client.deleteObject(bucketName, objPath);
            } catch(AmazonClientException ace) {
                ace.printStackTrace();

            }
            return this;
        });
    }

    @Override
    public Mono<S3Resource> select(String path) {
        return getSession().select(path);
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    private Stat onStat() {
        AmazonS3Client S3Client = getSession().s3Client;
        String path = getPath();
        if (getPath().equals("amazons3/")){
            List<Bucket> bucketList = S3Client.listBuckets();
            Stat bucketStat = buildRootStat(bucketList);
            return bucketStat;
        }
        String bucketName= path.split("/")[1];
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
                boolean isDir = path.substring(path.length() - 1).equals("/");
                String[] currpath = path.split("/");
                String objPath = "";
                for(int i = 2; i < currpath.length; i++){
                    objPath+=currpath[i];
                }
                String bucketName = currpath[1];
                GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objPath);
                ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
                overrides.setContentDisposition("attachment; filename=\"report.html\"");
                req.setResponseHeaders(overrides);
                URL url = getSession().s3Client.generatePresignedUrl(req);
                downloadLink = url.toString();

            } catch(AmazonClientException ace) {
                ace.printStackTrace();

            }
            return Mono.just(downloadLink);
    }

}
