package org.onedatashare.server.module.s3;

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

    private FileObject fileObject;

    protected S3Resource(S3Session session, String path, FileObject fileObject) {
        super(session, path);
        this.fileObject = fileObject;
    }

    /**
     * This method creates a directory with the name of the folder to be created
     * on clicking the 'New Folder' option on the front end .
     * @return current VfsResource instance
     */
    public Mono<S3Resource> mkdir() {
        return initialize().doOnSuccess(vfsResource -> {
            try {
                fileObject.createFolder();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        });
    }

    public Mono<S3Resource> delete() {
        try {
            fileObject.deleteAll();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }return Mono.just(this);
    }

    @Override
    public Mono<S3Resource> select(String path) {
        return getSession().select(path);
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    private Stat onStat() {
        Stat stat = new Stat();
        try {
            if(fileObject.isFolder()){
                stat.setDir(true);
                stat.setFile(false);
            }
            else {
                stat = fileContentToStat(fileObject);
            }
            stat.setName(fileObject.getName().getBaseName());

            if(stat.isDir()) {
                FileObject[] children = fileObject.getChildren();
                ArrayList<Stat> files = new ArrayList<>();
                for(FileObject file : children) {
                    files.add(fileContentToStat(file));
                }
                stat.setFiles(files);
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return stat;
    }

    public Stat fileContentToStat(FileObject file) {
        Stat stat = new Stat();
        FileContent fileContent = null;
        try {
            fileContent = file.getContent();
            if(file.isFolder()) {
                stat.setDir(true);
                stat.setFile(false);
            }
            else {
                stat.setFile(true);
                stat.setDir(false);
                stat.setSize(fileContent.getSize());
            }

            stat.setName(file.getName().getBaseName());
            stat.setTime(fileContent.getLastModifiedTime() / 1000);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return stat;
    }

    public VfsTap tap() {
        VfsTap vfsTap = new VfsTap();
        return vfsTap;
    }

    public VfsDrain sink() {
        return new VfsDrain().start();
    }

    public VfsDrain sink(Stat stat) {
        return new VfsDrain().start(getPath() + stat.getName());
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return initialize()
                .map(S3Resource::onStat)
                .map( s -> {
                    List<Stat> sub = new LinkedList<>();
                    long directorySize = 0L;

                    if(s.isDir()){
                        try {
                            directorySize = buildDirectoryTree(sub, fileObject.getChildren(), "/");
                        }
                        catch(FileSystemException fse){
                            ODSLoggerService.logError("Exception encountered while generating " +
                                    "file objects within a folder", fse);
                        }
                    }
                    else{
                        setFileResource(true);
                        sub.add(s);
                        directorySize = s.getSize();
                    }
                    s.setFilesList(sub);
                    s.setSize(directorySize);
                    return s;
                });
    }

    public Long buildDirectoryTree(List<Stat> sub, FileObject[] fileObjects, String relativeDirName){
        long directorySize = 0L;

        for(FileObject fileObject : fileObjects){
            try {
                if (fileObject.isFile()) {
                    Stat fileStat = fileContentToStat(fileObject);
                    fileStat.setName(relativeDirName + fileStat.getName());
                    directorySize += fileStat.getSize();
                    sub.add(fileStat);
                } else {
                    String dirName = fileObject.getName().toString();
                    dirName = relativeDirName + dirName.substring(dirName.lastIndexOf("/")+1) + "/";
                    directorySize += buildDirectoryTree(sub, fileObject.getChildren(), dirName);
                }
            }
            catch (FileSystemException e) {
                e.printStackTrace();

            }
        }
        return directorySize;
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
        FileObject drainFileObject = fileObject;

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
