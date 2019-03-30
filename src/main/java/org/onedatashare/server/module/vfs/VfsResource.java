package org.onedatashare.server.module.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.BaseStream;
import java.util.zip.ZipOutputStream;


public class VfsResource extends Resource<VfsSession, VfsResource> {

    private FileObject fileObject;

    protected VfsResource(VfsSession session, String path, FileObject fileObject) {
        super(session, path);
        this.fileObject = fileObject;
    }

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
                fileObject.delete();
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
        return initialize().map(VfsResource::onStat);
    }

    private Stat onStat() {
        Stat stat = new Stat();
        try {
            if (fileObject.isFolder()) {
                stat.dir = true;
                stat.file = false;
            } else {
                stat = fileContentToStat(fileObject);
            }
            stat.name = fileObject.getName().getBaseName();

            if (stat.dir) {
                FileObject[] children = fileObject.getChildren();
                ArrayList<Stat> files = new ArrayList<>();
                for (FileObject file : children) {
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
            if (file.isFolder()) {
                stat.dir = true;
                stat.file = false;
            } else {
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
        return new VfsTap();
    }

    public VfsDrain sink() {
        return new VfsDrain().start();
    }

    class VfsTap implements Tap {
        FileContent fileContent;

        {
            try {
                fileContent = fileObject.getContent();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        }

        final long size = stat().block().size;

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
                                finalInputStream.read(b, state, sliceSizeInt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                        } else {
                            int remaining = sizeInt - state;
                            byte[] b = new byte[remaining];
                            try {
                                finalInputStream.read(b, state, remaining);
                                finalInputStream.close();
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
        long uploaded = 0L;

        @Override
        public VfsDrain start() {
            try {
                fileObject.createFile();
                outputStream = fileObject.getContent().getOutputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            return this;
        }

        @Override
        public void drain(Slice slice) {
            try {
                outputStream.write(slice.asBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploaded += slice.length();
        }

        @Override
        public void finish() {
            try {
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
            downloadURL.append("ftp://" + username + '@' + password + ':' + downloadLink.substring(6));
        else
            downloadURL.append(downloadLink);
        downloadLink = downloadURL.toString();
        System.out.println("hello " + downloadLink);
        return Mono.just(downloadLink);
    }

    public FileObject getSftpObject() {
        return fileObject;
    }

}

