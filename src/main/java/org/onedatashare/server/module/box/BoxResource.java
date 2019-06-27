package org.onedatashare.server.module.box;

import com.box.sdk.*;
import com.box.sdk.http.HttpMethod;
import com.sun.mail.iap.ByteArray;
import org.apache.commons.io.IOUtils;
import org.onedatashare.server.model.core.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.util.*;

public class BoxResource extends Resource<BoxSession, BoxResource> {

    protected BoxResource(BoxSession session, String path, String id) {
        super(session, path, id);
    }
    protected BoxResource(BoxSession session, String path) {
        super(session, path);
    }


    //tap outgoing
    //drain incoming
    public Mono<Stat> stat() {

        return initialize().map(BoxResource::onStat);
    }

    public Stat onStat() {

        BoxFolder folder = null;

        BoxItem item;

       // folder.getMetadata().getTypeName();
        //BoxAPIConnection client = new BoxAPIConnection(session.client.getAccessToken());
        //URL url = new URL();
        //BoxAPIRequest req = new BoxAPIRequest(session.client, url, "GET");
        ;
        if (id == null){
           // folder = new BoxFolder(session.client, "0");
            folder = BoxFolder.getRootFolder(session.client);
            Iterable<BoxItem.Info> children = folder.getChildren();
            Stat rStat = buildDirStat(children);
            rStat.dir = true;
            rStat.file = false;
            rStat.name = "root";
            return rStat;
           // stat.id = folder.getID();
        }
        String type = "";
            try{
                folder = new BoxFolder(session.client, id);
                item = folder;
                type = item.getInfo().getType();
            }catch(BoxAPIResponseException e){
                item = new BoxFile(session.client, id);
                type = item.getInfo().getType();

            }

            if(type.equals("folder")) {
                Iterable<BoxItem.Info> children = folder.getChildren();
                Stat stat = buildDirStat(children);
                stat.dir = true;
                stat.file = false;
                stat.name = folder.getInfo().getName();
                return stat;
            }
            else{
                BoxFile file = new BoxFile(session.client, id);
                Stat stat = new Stat();
                stat.dir = false;
                stat.file = true;
                stat.name = file.getInfo().getName();
                stat.id = id;

                BoxFile.Info fileInfo = file.getInfo();
                stat.size = fileInfo.getSize();
                stat.time = fileInfo.getContentModifiedAt().getTime() / 1000;
                BoxSharedLink sharedLink = fileInfo.getSharedLink();
                if (sharedLink != null) {
                    stat.link = sharedLink.toString();
                }
                EnumSet<BoxFile.Permission> permissions = fileInfo.getPermissions();
                if (permissions != null) {
                    stat.perm = permissions.toString();
                }
                return stat;

            }
        }

    public Stat buildDirStat(Iterable<BoxItem.Info> children){
        Stat stat = new Stat();
        ArrayList<Stat> contents = new ArrayList<>();
        for (BoxItem.Info child : children) {

            Stat statChild = new Stat();
            statChild.file = child instanceof BoxFile.Info;
            statChild.dir = child instanceof BoxFolder.Info;
            statChild.setDir(statChild.dir);
            statChild.setFile(statChild.file);
            statChild.id = child.getID();
            statChild.name = child.getName();
            if (statChild.dir) {
                BoxFolder childFolder = new BoxFolder(session.client, statChild.id);
                BoxFolder.Info childFolderInfo = childFolder.getInfo();
                statChild.size = childFolderInfo.getSize();
                statChild.time = childFolderInfo.getContentModifiedAt().getTime() / 1000;
                BoxSharedLink sharedLink = childFolderInfo.getSharedLink();
                if (sharedLink != null) {
                    statChild.link = sharedLink.toString();
                }
                EnumSet<BoxFolder.Permission> permissions = childFolderInfo.getPermissions();
                if (permissions != null) {
                    statChild.perm = permissions.toString();
                }
                contents.add(statChild);
            }

            if (statChild.file) {
                BoxFile childFile = new BoxFile(session.client, statChild.id);
                BoxFile.Info childFileInfo = childFile.getInfo();
                statChild.size = childFileInfo.getSize();
                statChild.time = childFileInfo.getContentModifiedAt().getTime() / 1000;
                BoxSharedLink sharedLink = childFileInfo.getSharedLink();
                if (sharedLink != null) {
                    statChild.link = sharedLink.toString();
                }
                EnumSet<BoxFile.Permission> permissions = childFileInfo.getPermissions();
                if (permissions != null) {
                    statChild.perm = permissions.toString();
                }
                contents.add(statChild);
            }


        }

        stat.files = new Stat[contents.size()];

        stat.files = contents.toArray(stat.files);

        return stat;
    }

    public Mono<BoxResource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try {
                String[] currpath = path.split("/");
                String folderId = id;
                if(id == null){
                    folderId = "0";
                }
                BoxFolder parentFolder = new BoxFolder(resource.session.client, folderId);
                BoxFolder.Info childFolder = parentFolder.createFolder(currpath[currpath.length-1]);

                id = childFolder.getID();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Mono<BoxResource> delete() {
        return initialize().doOnSuccess(resource -> {
            try {
                if(onStat().isFile()) {
                    BoxFile file = new BoxFile(resource.session.client, id);
                    file.delete();
                }
                else if(onStat().isDir()){
                    boolean recursive = true;
                    BoxFolder folder = new BoxFolder(resource.session.client, id);
                    folder.delete(recursive);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Mono<String> download(){
        String url = "";
        try {
            BoxFile file = new BoxFile(session.client, id);
            url = file.getDownloadURL().toString();

        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.just(url);
    }

    @Override
    public Mono<BoxResource> select(String path) {
        return session.select(path);
    }

    @Override
    public Mono<Stat> getTransferStat() {

        return stat().map(s -> {
            List<Stat> sub = new LinkedList<>();
            long directorySize = 0L;
            if(s.isDir()){
                directorySize = buildDirectoryTree(sub, id, "/");
            }else{
                sub.add(s);
                directorySize = s.getSize();
            }

            s.setFilesList(sub);
            s.setSize(directorySize);
            return s;
        });
    }

    public Long buildDirectoryTree(List<Stat> sub, String curId, String relativePath){
        if (curId == null){
            curId = "0";
        }
        Long directorySize = 0L;
        try {
            BoxFolder folder = new BoxFolder(session.client, curId);
            for (BoxItem.Info itemInfo : folder) {
                if (itemInfo instanceof BoxFile.Info) {
                    BoxFile file = new BoxFile(session.client, itemInfo.getID());
                    BoxFile.Info fileInfo = file.getInfo();
                    directorySize += fileInfo.getSize();
                    Stat fStat = fileContentToStat(fileInfo);
                    fStat.setName( relativePath + fileInfo.getName());
                    sub.add(fStat);
                } else if (itemInfo instanceof BoxFolder.Info) {
                    BoxFolder fold = new BoxFolder(session.client, itemInfo.getID());
                    BoxFolder.Info folderInfo = fold.getInfo();
                    directorySize += buildDirectoryTree(sub, folderInfo.getID(), relativePath + folderInfo.getName() + "/");
                }
            }
        }
        catch (Exception e){
            System.out.println("Exception encountered while building directory tree");
            e.printStackTrace();
        }
        return directorySize;
    }

    public Stat fileContentToStat(BoxItem.Info info) {
        Stat stat = new Stat();
        try {
            if (info instanceof BoxFolder.Info) {
                stat.dir = true;
                stat.file = false;
            } else {
                stat.file = true;
                stat.dir = false;
                stat.size = info.getSize();
            }
            stat.id = info.getID();
            stat.name = info.getName();
            if (info.getContentModifiedAt() != null)
                stat.time = info.getContentModifiedAt().getTime() / 1000;
        } catch (BoxAPIException e) {
            e.printStackTrace();
        }
        return stat;
    }

    public BoxTap tap() {
        BoxTap boxTap = new BoxTap();
        return boxTap;
    }

    class BoxTap implements Tap {
        long size;
        BoxAPIConnection api = session.client;
        BoxAPIRequest req;

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
            BoxFile file = new BoxFile(api, stat.id);
            URL downloadUrl = file.getDownloadURL();
            try {
                req = new BoxAPIRequest(api, downloadUrl, HttpMethod.GET);
            }catch(Exception e){
                e.printStackTrace();
            }
            size = stat.getSize();
            return tap(sliceSize);

        }

        @Override
        public Flux<Slice> tap(long sliceSize) {

            int sliceSizeInt = Math.toIntExact(sliceSize);
            int sizeInt = Math.toIntExact(size);
            BoxAPIResponse resp = req.send();
            InputStream inputStream = resp.getBody();
            return Flux.generate(
                    () -> 0,
                    (state, sink) -> {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        if (state + sliceSizeInt < sizeInt) {
                            try {
                                IOUtils.copy(inputStream, outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(outputStream.toByteArray()));
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                IOUtils.copy(inputStream, outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            sink.next(new Slice(outputStream.toByteArray()));
                            sink.complete();
                        }
                        return state + sliceSizeInt;
                    });

        }


    }
    public BoxDrain sink() {
        return new BoxDrain().start();
    }

    public BoxDrain sink(Stat stat){
        return new BoxDrain().start(path + stat.getName());
    }

    class BoxDrain implements Drain {

        String drainPath = path;
        Boolean isDirTransfer = false;


        @Override
        public BoxDrain start(String drainPath) {
            this.drainPath = drainPath;
            this.isDirTransfer = true;
            return start();
        }

        @Override
        public BoxDrain start() {
            return null;
        }

        @Override
        public void drain(Slice slice) {

        }

        @Override
        public void finish() {

        }
    }
}
