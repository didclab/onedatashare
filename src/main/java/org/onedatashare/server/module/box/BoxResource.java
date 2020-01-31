package org.onedatashare.server.module.box;

import com.box.sdk.*;
import com.box.sdk.http.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.error.ODSAccessDeniedException;
import org.onedatashare.server.service.ODSLoggerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import org.onedatashare.server.model.core.ODSConstants;

import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.onedatashare.server.model.core.ODSConstants.BOX_URI_SCHEME;

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

        return Mono.just(onStat());
    }

    public Stat onStat() {

        BoxFolder folder = null;

        BoxItem item;

        if (getId() == null){
            folder = BoxFolder.getRootFolder(getSession().client);
            Iterable<BoxItem.Info> children = folder.getChildren();
            Stat rStat = buildDirStat(children);
            rStat.setDir(true);
            rStat.setFile(false);
            rStat.setName("root");
            EnumSet<BoxFolder.Permission> permissions = folder.getInfo().getPermissions();
            if (permissions != null) {
                rStat.setPermissions(permissions.toString());
            }
            return rStat;
        }
        String type = "";
        try{
            folder = new BoxFolder(getSession().client, getId());
            item = folder;
            type = item.getInfo().getType();
        }catch(BoxAPIResponseException e){
            item = new BoxFile(getSession().client, getId());
            type = item.getInfo().getType();

        }

        if(type.equals("folder")) {
            Iterable<BoxItem.Info> children = folder.getChildren();
            Stat stat = buildDirStat(children);
            stat.setDir(true);
            stat.setFile(false);
            stat.setName(folder.getInfo().getName());
            EnumSet<BoxFolder.Permission> permissions = folder.getInfo().getPermissions();
            if (permissions != null) {
                stat.setPermissions(permissions.toString());
            }
            return stat;
        }
        else{
            BoxFile file = new BoxFile(getSession().client, getId());
            Stat stat = new Stat();
            stat.setDir(false);
            stat.setFile(true);
            stat.setName(file.getInfo().getName());
            stat.setId(file.getID());

            BoxFile.Info fileInfo = file.getInfo();
            stat.setSize(fileInfo.getSize());
            stat.setTime(fileInfo.getContentModifiedAt().getTime() / 1000);
            BoxSharedLink sharedLink = fileInfo.getSharedLink();
            if (sharedLink != null) {
                stat.setLink(sharedLink.toString());
            }
            EnumSet<BoxFile.Permission> permissions = fileInfo.getPermissions();
            if (permissions != null) {
                stat.setPermissions(permissions.toString());
            }
            return stat;

        }
    }

    /**
     * Builds a new stat object containing information about a parent's children in
     * a case of a directory transfer
     * @author Javier Falca
     * @param children Takes in an Iterable Object of type BoxItem.Info from the parent Box Folder
     * @return Stat object with a directory built
     */
    public Stat buildDirStat(Iterable<BoxItem.Info> children){
        Stat stat = new Stat();
        ArrayList<Stat> contents = new ArrayList<>();
        for (BoxItem.Info child : children) {

            Stat statChild = new Stat();
            statChild.setFile(child instanceof BoxFile.Info);
            statChild.setDir(child instanceof BoxFolder.Info);
            statChild.setDir(statChild.isDir());
            statChild.setFile(statChild.isFile());
            statChild.setId(child.getID());
            statChild.setName(child.getName());
            if (statChild.isDir()) {
                BoxFolder childFolder = new BoxFolder(getSession().client, statChild.getId());
                BoxFolder.Info childFolderInfo = childFolder.getInfo();
                statChild.setSize(childFolderInfo.getSize());
                statChild.setTime(childFolderInfo.getContentModifiedAt().getTime() / 1000);
                BoxSharedLink sharedLink = childFolderInfo.getSharedLink();
                if (sharedLink != null) {
                    statChild.setLink(sharedLink.toString());
                }
                EnumSet<BoxFolder.Permission> permissions = childFolderInfo.getPermissions();
                if (permissions != null) {
                    statChild.setPermissions(permissions.toString());
                }
                contents.add(statChild);
            }

            if (statChild.isFile()) {
                BoxFile childFile = new BoxFile(getSession().client, statChild.getId());
                BoxFile.Info childFileInfo = childFile.getInfo();
                statChild.setSize(childFileInfo.getSize());
                statChild.setTime(childFileInfo.getContentModifiedAt().getTime() / 1000);
                BoxSharedLink sharedLink = childFileInfo.getSharedLink();
                if (sharedLink != null) {
                    statChild.setLink(sharedLink.toString());
                }
                EnumSet<BoxFile.Permission> permissions = childFileInfo.getPermissions();
                if (permissions != null) {
                    statChild.setPermissions(permissions.toString());
                }
                contents.add(statChild);
            }


        }


        stat.setFiles(new Stat[contents.size()]);

        stat.setFiles(contents.toArray(stat.getFiles()));

        return stat;
    }

    public Mono<BoxResource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try {
                String[] currpath = getPath().split("/");
                String folderId = getId();
                if(getId() == null){
                    folderId = "0";
                }
                BoxFolder parentFolder = new BoxFolder(resource.getSession().client, folderId);
                BoxFolder.Info childFolder = parentFolder.createFolder(currpath[currpath.length-1]);

                setId(childFolder.getID());
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Mono<BoxResource> delete() {
        return Mono.just(this).map(resource -> {
            try {
                if(onStat().isFile()) {
                    BoxFile file = new BoxFile(resource.getSession().client, getId());
                    file.delete();
                }
                else if(onStat().isDir()){
                    boolean recursive = true;
                    BoxFolder folder = new BoxFolder(resource.getSession().client, getId());
                    folder.delete(recursive);
                }
            }catch(BoxAPIResponseException be){
                if(be.getResponseCode() == 403){
                    throw new ODSAccessDeniedException(403);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return this;
        });

    }

    public Mono<String> download(){
        String url = "";
        try {
            BoxFile file = new BoxFile(getSession().client, getId());
            url = file.getDownloadURL().toString();

        }catch(BoxAPIResponseException be){
            if(be.getResponseCode() == 403){
                return Mono.error(new ODSAccessDeniedException(403));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.just(url);
    }

    @Override
    public Mono<BoxResource> select(String path) {
        return getSession().select(path);
    }

    @Override
    public Mono<Stat> getTransferStat() {

        return stat().map(s -> {
            List<Stat> sub = new LinkedList<>();
            long directorySize = 0L;
            if(s.isDir()){
                directorySize = buildDirectoryTree(sub, getId(), "/");
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
            BoxFolder folder = new BoxFolder(getSession().client, curId);
            for (BoxItem.Info itemInfo : folder) {
                if (itemInfo instanceof BoxFile.Info) {
                    BoxFile file = new BoxFile(getSession().client, itemInfo.getID());
                    BoxFile.Info fileInfo = file.getInfo();
                    directorySize += fileInfo.getSize();
                    Stat fStat = fileContentToStat(fileInfo);
                    fStat.setName( relativePath + fileInfo.getName());
                    sub.add(fStat);
                } else if (itemInfo instanceof BoxFolder.Info) {
                    BoxFolder fold = new BoxFolder(getSession().client, itemInfo.getID());
                    BoxFolder.Info folderInfo = fold.getInfo();
                    directorySize += buildDirectoryTree(sub, folderInfo.getID(), relativePath + folderInfo.getName() + "/");
                }
            }
        }
        catch (Exception e){
            ODSLoggerService.logError("Exception encountered while building directory tree");
            e.printStackTrace();
        }
        return directorySize;
    }

    /**
     * Fills in information for a Stat object
     * @author Javier Falca
     * @param info Box Item refers to a Box File or Box Folder, a stat is created
     * with the information provided on the Info class
     * @return Stat object
     */
    public Stat fileContentToStat(BoxItem.Info info) {
        Stat stat = new Stat();
        try {
            if (info instanceof BoxFolder.Info) {
                stat.setDir(true);
                stat.setFile(false);
            } else {
                stat.setFile(true);
                stat.setDir(false);
                stat.setSize(info.getSize());
            }
            stat.setId(info.getID());
            stat.setName(info.getName());
            if (info.getContentModifiedAt() != null)
                stat.setTime(info.getContentModifiedAt().getTime() / 1000);
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
        BoxAPIConnection api = getSession().client;
        BoxAPIRequest req;

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
            BoxFile file = new BoxFile(api, stat.getId());
            try {
                URL downloadUrl = file.getDownloadURL();
                req = new BoxAPIRequest(api, downloadUrl, HttpMethod.GET);
            }catch(BoxAPIResponseException be){
                if(be.getResponseCode() == 403){
                    return Flux.error(be);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            size = stat.getSize();
            return tap(sliceSize);

        }

        /**
         * BoxTap follows a similar model to Google Drive and other transfer modules
         * It uses input and output streams to perform the outgoing transfer
         * @author Javier Falca
         * @param sliceSize
         * @return A flux generated slice
         */
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

    public BoxDrain sink(Stat stat, boolean isDir){
        String path = isDir ? getPath() + stat.getName() : getPath();
        return new BoxDrain().start(path, stat.getSize(), isDir);
    }

    /**
     * @README
     * @Author Javier Falca
     * Box Chunked Upload has some cryptic properties that are not too well documented
     * 1.) To perform a chunked upload, a file must be greater than 20MB, any less will have to be
     * sent as a single chunk with a different function.
     * 2.) Each chunk must be exactly 8MB in size, if this number is not met, the chunk will fail to upload.
     * 3.) A SHA-1 Base64 hash of the entire file must be provided at the end of the finish state during the commit.
     */
    private HashMap<String, String> hashMap = new HashMap<String, String>();
    class BoxDrain implements Drain {
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        long totalSize = 0;
        long sizeUploaded = 0;
        int part_size;
        String fileName;

        String drainPath = getPath();
        Boolean isDirTransfer = false;


        BoxFileUploadSession.Info sessionInfo;
        BoxFileUploadSession session;

        MessageDigest sha1;
        List<BoxFileUploadSessionPart> parts;

        ByteArrayInputStream smallFileStream;
        boolean isSmall;

        public BoxDrain start(String drainPath, long size, boolean isDir){
            totalSize = size;
            this.isDirTransfer = isDir;
            isSmall = (totalSize < 20971520) ? true : false;
            try{
                sha1 = MessageDigest.getInstance("SHA-1");
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return start(drainPath);
        }

        @Override
        public BoxDrain start(String drainPath) {
            this.drainPath = drainPath;
            return start();
        }

        @Override
        public BoxDrain start() {

            String name = drainPath.substring(drainPath.lastIndexOf('/')+1);

            fileName = name;
            try {
                String parentid = getSession().idMap.get(getSession().idMap.size()-1).getId();
                if( parentid != null ) {
                    setId( getSession().idMap.get(getSession().idMap.size()-1).getId() );
                }else {
                    parentid = "0";
                    setId("0");
                }

                if(isDirTransfer) {
                    String path = drainPath.substring(BOX_URI_SCHEME.length(), drainPath.lastIndexOf("/"));
                    if(!hashMap.containsKey(path)){
                        //directory has not been created in previous iterations
                        String[] folders = path.split("/");
                        BoxFolder parentFolder = new BoxFolder(getSession().client, parentid);
                        for(String folder : folders){
                            if(!folder.equals(parentFolder.getInfo().getName())) {
                                BoxFolder.Info childFolder = parentFolder.createFolder(folder);
                                parentid = childFolder.getID();
                                parentFolder = childFolder.getResource();
                            }
                        }
                        hashMap.put(path, parentid);
                        setId(parentid);
                    }else {
                        parentid = hashMap.get(path);
                        setId(parentid);
                    }
                }

                BoxFolder folder = new BoxFolder(getSession().client, parentid);
                if(!isSmall){
                    try {
                        sessionInfo = folder.createUploadSession(name, totalSize);
                        parts = new ArrayList<BoxFileUploadSessionPart>();
                        session = sessionInfo.getResource();
                    } catch(Exception e){
                        //Already Exists
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return this;
        }

        @Override
        public void drain(Slice slice) {
            try {
                //Box only allows chunked upload for files greater than 20MB at 8MB chunks
                if (isSmall) {
                    chunk.write(slice.asBytes());
                } else {
                    try {
                        part_size = sessionInfo.getPartSize();
                    } catch(NullPointerException npe){

                    }
                    chunk.write(slice.asBytes());
                    if (chunk.size() == part_size) {
                        try {
                            BoxFileUploadSessionPart part = session.uploadPart(chunk.toByteArray(), sizeUploaded, chunk.size(), totalSize);
                            parts.add(part);
                            sizeUploaded = sizeUploaded + chunk.size();
                            sha1.update(chunk.toByteArray());
                            chunk = new ByteArrayOutputStream();
                        }catch(Exception e){
                            //Part already exists
                        }
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }


        @Override
        public void finish() {
            try {
                if (isSmall) {
                    BoxFolder folder = new BoxFolder(getSession().client, getId());
                    Iterable<BoxItem.Info> children = folder.getChildren();
                    for (BoxItem.Info child : children) {
                        if(child.getName().equals(fileName)){
                            return;
                        }
                    }

                    smallFileStream = new ByteArrayInputStream(chunk.toByteArray());
                    BoxFile.Info smallFile = folder.uploadFile(smallFileStream, fileName);
                    smallFileStream.close();

                } else {
                    if (chunk.size() > 0) {
                        try {
                            BoxFileUploadSessionPart part = session.uploadPart(chunk.toByteArray(), sizeUploaded, chunk.size(), totalSize);
                            parts.add(part);
                            sizeUploaded = sizeUploaded + chunk.size();
                            sha1.update(chunk.toByteArray());
                        }catch(Exception e){

                        }
                    }

                    byte[] digestBytes = sha1.digest();

                    //Base64 encoding of the hash
                    String digestStr = Base64.getEncoder().encodeToString(digestBytes);
                    try {
                        BoxFile.Info largeFile = session.commit(digestStr, parts, null, null, null);
                    }catch(Exception e){
                        
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
