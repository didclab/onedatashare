package org.onedatashare.server.module.box;

import com.box.sdk.*;
import com.box.sdk.http.HttpMethod;
import com.sun.mail.iap.ByteArray;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.service.ODSLoggerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        if (getId() == null){
            folder = BoxFolder.getRootFolder(getSession().client);
            Iterable<BoxItem.Info> children = folder.getChildren();
            Stat rStat = buildDirStat(children);
            rStat.setDir(true);
            rStat.setFile(false);
            rStat.setName("root");
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
        return initialize().doOnSuccess(resource -> {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Mono<String> download(){
        String url = "";
        try {
            BoxFile file = new BoxFile(getSession().client, getId());
            url = file.getDownloadURL().toString();

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
            System.out.println("Exception encountered while building directory tree");
            e.printStackTrace();
        }
        return directorySize;
    }

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

    public BoxDrain sink(Stat stat, boolean isDir){
        String path = isDir ? getPath() + stat.getName() : getPath();
        return new BoxDrain().start(path, stat.getSize());
    }

    class BoxDrain implements Drain {
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        long totalSize = 0;
        long sizeUploaded = 0;
        int part_size;
        String resumableSessionURL;
        String drainPath = getPath();
        Boolean isDirTransfer = false;

        MessageDigest sha1;

        public BoxDrain start(String drainPath, long size){
             totalSize = size;
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
            this.isDirTransfer = true;
            return start();
        }

        @Override
        public BoxDrain start() {
            String name[] = drainPath.split("/");
            try {
                String parentid = getSession().idMap.get(getSession().idMap.size()-1).getId();
                if( parentid != null ) {
                    setId( getSession().idMap.get(getSession().idMap.size()-1).getId() );
                }else {
                    parentid = "0";
                    setId("0");
                }

                URL url = new URL("https://upload.box.com/api/2.0/files/upload_sessions");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();

                request.setConnectTimeout(15000);
                request.setUseCaches(false);
                request.setRequestMethod("POST");
                request.setDoInput(true);
                request.setDoOutput(true);

                request.setRequestProperty("Authorization", "Bearer " + getSession().client.getAccessToken());
                request.setRequestProperty("Content-Type", "application/json");
                String body = "{\"folder_id\": \"" + getId() + "\", \"file_size\": " + totalSize + ", \"file_name\": \"" + name[name.length - 1] + "\"}";

                OutputStream outputStream = request.getOutputStream();
                outputStream.write(body.getBytes());
                outputStream.flush();
                outputStream.close();
                request.connect();
                System.out.println(request.toString());

                int uploadRequestResponseCode =  request.getResponseCode();
                System.out.println(request.getResponseCode() + request.getResponseMessage());
                if(uploadRequestResponseCode == 200 || uploadRequestResponseCode == 201){
                    BufferedReader reader = new BufferedReader(new InputStreamReader((request.getInputStream())));
                    String output = reader.readLine();
                    String jsonString = output;
                    System.out.println(jsonString);

                    ObjectMapper mapper = new ObjectMapper();
                    Map<String,Object> map = mapper.readValue(jsonString, Map.class);
                    Map<String,Object> sessionEndpointsMap = (Map<String, Object>) map.get("session_endpoints");
                    resumableSessionURL = sessionEndpointsMap.get("upload_part").toString();
                    part_size = Integer.parseInt(map.get("part_size").toString());
                    System.out.println(resumableSessionURL);
                }
                else{
                    throw new Exception("Error occurred while transferring into Box");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return this;
        }

        private String hashString(byte[] input) {
            sha1.update(input);
            byte[] messageDigest = sha1.digest();
            return Base64.getEncoder().encodeToString(messageDigest);
        }

        @Override
        public void drain(Slice slice) {
            try {
                chunk.write(slice.asBytes());
                int chunks = chunk.size() / part_size;
                int sizeUploading = Math.min(chunks * part_size, part_size);
                if (sizeUploading > 0) {
                    URL url = new URL(resumableSessionURL);
                    //if(sizeUploading > 0) {
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.setRequestMethod("PUT");
                    request.setDoOutput(true);
                    request.setRequestProperty("authorization", "bearer " + getSession().client.getAccessToken());
                    request.setRequestProperty("content-range", "bytes " + sizeUploaded + "-" + (sizeUploaded + sizeUploading - 1) + "/" + totalSize);

                    System.out.println(sizeUploaded + " " + sizeUploading + " " + totalSize);
                    System.out.println("content-range: " +  "bytes " + sizeUploaded + "-" + (sizeUploaded + sizeUploading - 1) + "/" + totalSize);

                    request.setRequestProperty("content-type", "application/octet-stream");
                    byte[] newbuffer = new byte[sizeUploading];
                    chunk.write(newbuffer, 0, sizeUploading);
                    String sliceHash = hashString(newbuffer);
                    System.out.println("sha=" + sliceHash);
                    request.setRequestProperty("digest", "sha=" + sliceHash);

                    OutputStream outputStream = request.getOutputStream();
                    outputStream.write(newbuffer);
                    outputStream.close();
                    request.connect();
                    int requestCode = request.getResponseCode();
                    System.out.println(request.getResponseMessage());

                    System.out.println(request.toString());
//                    byte[] op = new byte[999];
//                    System.out.println(request.getInputStream().read(op) + new String(op));
                    if (requestCode == 200) {
                        sizeUploaded = sizeUploaded + sizeUploading;
                        ByteArrayOutputStream temp = new ByteArrayOutputStream();
                        temp.write(chunk.toByteArray(), sizeUploading, (chunk.size() - sizeUploading));
                        chunk = temp;
                    } else {
                        ODSLoggerService.logDebug("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void finish() {
            try{
                URL url = new URL(resumableSessionURL);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("PUT");
                request.setConnectTimeout(10000);
                request.setRequestProperty("Content-Length", Long.toString(chunk.size()));
//                if(chunk.size() == 0)
//                    request.setRequestProperty("Content-Range", "bytes */" + (size+chunk.size()));
//                else
//                    request.setRequestProperty("Content-Range", "bytes " + size + "-" + (size+chunk.size()-1) + "/" + (size + chunk.size()));
                request.setDoOutput(true);
                OutputStream outputStream = request.getOutputStream();
                outputStream.write(chunk.toByteArray(), 0, chunk.size());
                outputStream.close();
                request.connect();
                if(request.getResponseCode() == 200 || request.getResponseCode() == 201){
                    ODSLoggerService.logDebug("code: " + request.getResponseCode()+
                            ", message: "+ request.getResponseMessage());
                }else {
                    ODSLoggerService.logDebug("code: " + request.getResponseCode()+
                            ", message: "+ request.getResponseMessage());
                    ODSLoggerService.logDebug("fail");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
