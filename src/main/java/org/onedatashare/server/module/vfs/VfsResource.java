package org.onedatashare.server.module.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
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

public class VfsResource extends Resource<VfsSession, VfsResource> {

  private FileObject fileObject;

  protected VfsResource(VfsSession session, String path, FileObject fileObject) {
    super(session, path);
    this.fileObject = fileObject;
  }

    /**
     * This method creates a directory with the name of the folder to be created
     * on clicking the 'New Folder' option on the front end .
     * @return current VfsResource instance
     */
    public Mono<VfsResource> mkdir() {
        return initialize().doOnSuccess(vfsResource -> {
            try {
                fileObject.createFolder();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        });
    }

    public Mono<VfsResource> delete() {
        return initialize().doOnSuccess(vfsResource -> {
            try {
                fileObject.deleteAll();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Mono<VfsResource> select(String path) {
        return session.select(path);
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    private Stat onStat() {
        Stat stat = new Stat();
        try {
            if(fileObject.isFolder()){
                stat.dir = true;
                stat.file = false;
            }
            else {
                stat = fileContentToStat(fileObject);
            }
            stat.name = fileObject.getName().getBaseName();

            if(stat.dir) {
                FileObject[] children = fileObject.getChildren();
                ArrayList<Stat> files = new ArrayList<>();
                for(FileObject file : children) {
                    files.add(fileContentToStat(file));
                }
                stat.setFiles(files);
            }
//            stat.setFiles();
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
                stat.dir = true;
                stat.file = false;
            }
            else {
                stat.file = true;
                stat.dir = false;
                stat.size = fileContent.getSize();
            }
            stat.name = file.getName().getBaseName();
            stat.time = fileContent.getLastModifiedTime() / 1000;
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
        return new VfsDrain().start(path + stat.getName());
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return initialize()
                .map(VfsResource::onStat)
                .map( s -> {
                    List<Stat> sub = new LinkedList<>();
                    long directorySize = 0L;

                    if(s.isDir()){
                        try {
                            directorySize = buildDirectoryTree(sub, fileObject.getChildren(), "/");
                        }
                        catch(FileSystemException fse){
                            System.out.println("Exception encountered while generating file objects within a folder");
                            fse.printStackTrace();
                        }
                    }
                    else{
                        fileResource = true;
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

    class VfsTap implements Tap {
        FileContent fileContent;
        long size;

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
            String downloadPath = path;
            if (!fileResource)
                downloadPath += stat.getName();
            try {
                fileContent = session.fileSystemManager.resolveFile(downloadPath, session.fileSystemOptions).getContent();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            size = stat.getSize();
            return tap(sliceSize);
        }


        public Flux<Slice> tap(long sliceSize) {
            int sliceSizeInt = Math.toIntExact(sliceSize);
            int sizeInt = Math.toIntExact(size);
            InputStream inputStream = null;
            try {
                inputStream = fileContent.getInputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            InputStream finalInputStream = inputStream;
            return Flux.generate(
                    () -> 0,
                    (state, sink) -> {
                        if (state + sliceSizeInt < sizeInt) {
                            byte[] b = new byte[sliceSizeInt];
                            try {
                                finalInputStream.read(b, 0, sliceSizeInt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                        } else {
                            int remaining = sizeInt - state;
                            byte[] b = new byte[remaining];
                            try {
                                finalInputStream.read(b, 0, remaining);
//                                finalInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                            sink.complete();
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
            try {
                drainFileObject.createFile();
                outputStream = drainFileObject.getContent().getOutputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            return this;
        }

        @Override
        public VfsDrain start(String drainPath) {
            try {
                drainFileObject = session.fileSystemManager.resolveFile(
                        drainPath.substring(0, drainPath.lastIndexOf('/')), session.fileSystemOptions);
                drainFileObject.createFolder();    // creates the folders for folder transfer
                drainFileObject = session.fileSystemManager.resolveFile(drainPath, session.fileSystemOptions);
                return start();
            }
            catch(FileSystemException fse){
                System.out.println("Exception encountered while creating file object");
                fse.printStackTrace();
            }
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
        String downloadLink = session.getUri().toString();
        UserInfoCredential userInfoCredential = (UserInfoCredential) session.credential;
        String username = userInfoCredential.getUsername(), password = userInfoCredential.getPassword();
        StringBuilder downloadURL = new StringBuilder();
        System.out.println(session + " " + username);
        if (username != null)
            downloadURL.append("ftp://" + username + ':' + password + '@' + downloadLink.substring(6));
        else
            downloadURL.append(downloadLink);
        downloadLink = downloadURL.toString();
        return Mono.just(downloadLink);
    }

    public Mono<ResponseEntity> getSftpObject() {

        InputStream inputStream = null;
        try {
            inputStream = fileObject.getContent().getInputStream();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        String[] strings = fileObject.getName().toString().split("/");
        String filename = strings[strings.length - 1];
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + filename);
        return Mono.just(ResponseEntity.ok()
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(inputStreamResource));
     }
}
