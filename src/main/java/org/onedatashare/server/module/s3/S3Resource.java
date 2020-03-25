package org.onedatashare.server.module.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
        return null;
    }

    public Mono<S3Resource> delete() {
        return null;
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
            List<Bucket> bucketList = S3Client.listBuckets();
            Stat bucketStat = buildBucketStat(bucketList);
            for (Bucket bucket : bucketList) {
                ObjectListing listing = S3Client.listObjects(bucket.getName());
            }
            return bucketStat;

    }

    public Stat buildBucketStat(List<Bucket> bucketList){
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

    public Stat buildDirStat(List<S3ObjectSummary> listing){
        Stat stat = new Stat();
        ArrayList<Stat> contents = new ArrayList<>();
        for(int i = 0; i < listing.size(); i++){
            S3ObjectSummary object = listing.get(i);
            Stat statChild = new Stat();
            if (object.getKey().endsWith("/")){
                statChild.setFile(false);
                statChild.setDir(true);
            }
            else{
                statChild.setFile(true);
                statChild.setDir(false);
                statChild.setSize(object.getSize());
            }
            statChild.setName(object.getKey());
            statChild.setPermissions(object.getOwner().getDisplayName());
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

    public Mono<String> getDownloadURL() {
       return null;
    }

}
