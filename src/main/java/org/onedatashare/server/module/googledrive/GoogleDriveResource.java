package org.onedatashare.server.module.googledrive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.IOUtils;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.NotFound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class GoogleDriveResource extends Resource<GoogleDriveSession, GoogleDriveResource> {

    public static final String ROOT_DIR_ID = "root";

    protected GoogleDriveResource(GoogleDriveSession session, String path, String id) {
        super(session, path, id);
    }
    protected GoogleDriveResource(GoogleDriveSession session, String path) {
        super(session, path,null);
    }

    public Mono<GoogleDriveResource> mkdir() {
        return Mono.create(s -> {
            try {
                String[] currpath = path.split("/");
                for(int i =0; i<currpath.length; i++){
//                    System.out.println("Parent ID: " + id);
                    File fileMetadata = new File();
                    fileMetadata.setName(currpath[i]);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(id));
                    File file = session.service.files().create(fileMetadata)
                            .setFields("id")
                            .execute();
                    System.out.println("Folder ID: " + file.getId());
                    id = file.getId();
                }
            } catch (IOException e) {
                e.printStackTrace();
                s.error(e);
            }
            s.success(this);
        });
    }

    public String mkdir(String directoryTree[]){
        String curId = id;

        for(int i=1; i< directoryTree.length-1; i++){
            String exisitingID = folderExistsCheck(curId, directoryTree[i]);
            if(exisitingID == null){
                try {
                    File fileMetadata = new File();
                    fileMetadata.setName(directoryTree[i]);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(curId));
                    File file = session.service.files().create(fileMetadata)
                            .setFields("id")
                            .execute();
                    curId = file.getId();

                } catch (IOException ioe) {
                    System.out.println("Exception encountered while creating directory tree");
                    ioe.printStackTrace();
                }
            }
            else{
                curId = exisitingID;
            }
        }
        return curId;
    }

    public String folderExistsCheck(String curId, String directoryName){

        try {
            String query = new StringBuilder().append("trashed=false and ").append("'" + curId + "'")
                                              .append(" in parents").toString();

            Drive.Files.List request = session.service.files().list()
                        .setOrderBy("name").setQ(query)
                        .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");
            FileList fileSet = null;
            List<File> fileList = null;

            do{
                fileSet = request.execute();
                fileList = fileSet.getFiles();

                for(File file : fileList){
                    if (file.getMimeType().equals("application/vnd.google-apps.folder")
                            && file.getName().equals(directoryName)) {
                        return file.getId();
                    }
                }
                request.setPageToken(fileSet.getNextPageToken());
            }while(request.getPageToken() != null);
        }
        catch (IOException ioe){
            System.out.println("Exception encountered while checking if folder " + directoryName + " exists in " + curId);
            ioe.printStackTrace();
        }
        return null;
    }

    public Mono<GoogleDriveResource> delete() {
       return Mono.create(s -> {
           try {
               this.session.service.files().delete(id).execute();
               id = session.idMap.get(session.idMap.size() - 1).getId();
               if(id == null && session.idMap.size() ==1)
                   id = ROOT_DIR_ID;
           } catch (Exception e) {
               s.error(e);
           }
           s.success(this);
       });
    }

    public Mono<String> download(){
        String downloadUrl ="";
        try {
            downloadUrl = "https://drive.google.com/uc?id="+id+"&export=download";

        }catch(Exception exp){
            System.out.println("Error encountered while generating shared link for " + path);
            System.out.println(exp);
        }
        return Mono.just(downloadUrl);
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    public Stat onStat() {
        Drive.Files.List result ;
        Stat stat = new Stat();
        stat.setName(path);
        stat.setId(id);

        try {
            if (path.equals("/")) {
                stat.setDir(true);
                result = session.service.files().list()
                    .setOrderBy("name")
                    .setQ("trashed=false and 'root' in parents")
                    .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");

                if (result == null)
                    throw new NotFound();

                FileList fileSet = null;
                List<Stat> sub = new LinkedList<>();
                do {
                    try {
                        fileSet = result.execute();
                        List<File> files = fileSet.getFiles();
                        for (File file : files) {
                            sub.add(mDataToStat(file));
                        }
                        stat.setFiles(sub);
                        result.setPageToken(fileSet.getNextPageToken());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileSet.setNextPageToken(null);
                    }
                }
                while (result.getPageToken() != null);
            } else {
                try {
                    File googleDriveFile = session
                            .service
                            .files()
                            .get(id)
                            .setFields("id, name, kind, mimeType, size, modifiedTime")
                            .execute();
                    if (googleDriveFile.getMimeType().equals("application/vnd.google-apps.folder")) {
                        stat.setDir(true);

                        String query = new StringBuilder().append("trashed=false and ")
//                          .append("'0BzkkzI-oRXwxfjRHVXZxQmhSaldCWWJYX0Y2OVliTkFLbjdzVTBFaWZ5c1RJRF9XSjViQ3c'")
                                .append("'" + id + "'")
                                .append(" in parents").toString();
                        result = session.service.files().list()
                                .setOrderBy("name")
                                .setQ(query)
                                .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");
                        if (result == null)
                            throw new NotFound();

                        FileList fileSet = null;

                        List<Stat> sub = new LinkedList<>();
                        do {
                            try {
                                fileSet = result.execute();
                                List<File> files = fileSet.getFiles();
                                for (File file : files) {
                                    sub.add(mDataToStat(file));
                                }
                                stat.setFiles(sub);
                                result.setPageToken(fileSet.getNextPageToken());
                            } catch (NullPointerException e) {

                            } catch (Exception e) {
                                fileSet.setNextPageToken(null);
                            }
                        }
                        while (result.getPageToken() != null);
                    } else {
                        stat.setFile(true);
                        stat.setTime(googleDriveFile.getModifiedTime().getValue() / 1000);
                        stat.setSize(googleDriveFile.getSize());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return stat;
    }

    public Mono<Stat> getTransferStat(){
        return stat().map(s ->{
                   List<Stat> sub = new LinkedList<>();
                    long directorySize = 0L;

                    if(s.isDir()){
                        directorySize = buildDirectoryTree(sub, id, "/");
                    }
                    else{
                        sub.add(s);
                        directorySize = s.getSize();
                    }
                    s.setFilesList(sub);
                    s.setSize(directorySize);
                    return s;
                });
    }

    public Long buildDirectoryTree(List<Stat> sub, String curId, String relativePath){
        Long directorySize = 0L;
        try {
            String query = new StringBuilder().append("trashed=false and ").append("'" + curId + "'")
                    .append(" in parents").toString();

            Drive.Files.List request = session.service.files().list()
                    .setOrderBy("name").setQ(query)
                    .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");
            FileList fileSet = null;
            List<File> fileList = null;

            do{
                fileSet = request.execute();
                fileList = fileSet.getFiles();

                for(File file : fileList){
                    if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                        directorySize += buildDirectoryTree(sub, file.getId(), relativePath + file.getName() + "/");
                    }
                    else{
                        Stat fileStat = mDataToStat(file);
                        fileStat.setName( relativePath + file.getName());
                        directorySize += fileStat.getSize();
                        sub.add(fileStat);
                    }
                }
                request.setPageToken(fileSet.getNextPageToken());
            }while(request.getPageToken() != null);
        }
        catch (IOException ioe){
            System.out.println("Exception encountered while building directory tree");
            ioe.printStackTrace();
        }
        return directorySize;
    }

    private Stat mDataToStat(File file) {
        Stat stat = new Stat(file.getName());

        try {
            stat.setFile(true);
            stat.setId(file.getId());
            stat.setTime(file.getModifiedTime().getValue()/1000);
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                stat.setDir(true);
                stat.setFile(false);
            }
            else if(file.containsKey("size"))
                stat.setSize(file.getSize());
        }
        catch (NullPointerException  e) {

        }catch (Exception  e) {
          e.printStackTrace();
        }
        return stat;
    }

    public GoogleDriveTap tap() {
        GoogleDriveTap gDriveTap = new GoogleDriveTap();
        return gDriveTap;
    }

    class GoogleDriveTap implements Tap {
        long size;
        Drive drive = session.service;
        com.google.api.client.http.HttpRequest httpRequestGet;

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {

            String downloadUrl = "https://www.googleapis.com/drive/v3/files/"+stat.getId()+"?alt=media";
            try {
                httpRequestGet = drive.getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl));
            } catch (IOException e) {
                e.printStackTrace();
            }
            size = stat.getSize();
           return tap(sliceSize);
        }

        @Override
        public Flux<Slice> tap(long sliceSize) {
            return Flux.generate(
                () -> 0L,
                (state, sink) -> {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    if (state + sliceSize < size) {
                        try {
                            httpRequestGet.getHeaders().setRange("bytes=" + state + "-" + (state + sliceSize - 1));
                            com.google.api.client.http.HttpResponse response = httpRequestGet.execute();
                            InputStream is = response.getContent();
                            IOUtils.copy(is, outputStream);
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
                            httpRequestGet.getHeaders().setRange("bytes=" + state + "-" + (state + size - state - 1));
                            com.google.api.client.http.HttpResponse response = httpRequestGet.execute();
                            InputStream is = response.getContent();
                            IOUtils.copy(is, outputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sink.next(new Slice(outputStream.toByteArray()));
                        sink.complete();
                    }
                    return state + sliceSize;
                });
        }
    }

    public GoogleDriveDrain sink() {
        return new GoogleDriveDrain().start();
    }

    public GoogleDriveDrain sink(Stat stat){
        return new GoogleDriveDrain().start(path + stat.getName());
    }

    class GoogleDriveDrain implements Drain {
//        long bytesWritten = 0;
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        long size = 0;
        String resumableSessionURL;
//        String upload_id;

        String drainPath = path;
        Boolean isDirTransfer = false;

        @Override
        public GoogleDriveDrain start(String drainPath) {
            this.drainPath = drainPath;
            this.isDirTransfer = true;
            return start();
        }

        @Override
        public GoogleDriveDrain start() {
            try{
                String parentid = session.idMap.get(session.idMap.size()-1).getId();
                if( parentid != null ) {
                    id = session.idMap.get(session.idMap.size()-1).getId();
                }else {
                    id = ROOT_DIR_ID;
                }
                String name[] = drainPath.split("/");

                if(isDirTransfer)
                    id = mkdir(name);

                URL url = new URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("POST");
                request.setDoInput(true);
                request.setDoOutput(true);
                request.setRequestProperty("Authorization", "Bearer " + ((OAuthCredential)(session.getCredential())).getToken());
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body;
                if(id !=null){
                     body = "{\"name\": \"" + name[name.length-1] + "\", \"parents\": [\"" + id + "\"]}";
                }else{
                     body = "{\"name\": \"" + name[name.length-1] + "\"}";
                }
               // request.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
                request.setRequestProperty("Content-Length", String.format(Locale.ENGLISH, "%d", body.getBytes().length));

                OutputStream outputStream = request.getOutputStream();
                outputStream.write(body.getBytes());
                outputStream.close();
                request.connect();
                int uploadRequestResponseCode  = request.getResponseCode();
                if(uploadRequestResponseCode == 200) {
                   resumableSessionURL = request.getHeaderField("location");
                }else{
                    throw new Exception("Transfer will fail");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        @Override
        public void drain(Slice slice) {
            try{
                chunk.write(slice.asBytes());
                int chunks = chunk.size() / (1<<18);
                int sizeUploading = chunks * (1<<18);
                URL url = new URL(resumableSessionURL);
                if(sizeUploading > 0) {
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.setRequestMethod("PUT");
                    request.setConnectTimeout(10000);
                    request.setRequestProperty("Content-Length", Long.toString(sizeUploading));
                    request.setRequestProperty("Content-Range", "bytes " + size + "-" + (size + sizeUploading - 1) + "/" + "*");
                    request.setDoOutput(true);
                    OutputStream outputStream = request.getOutputStream();
                    outputStream.write(chunk.toByteArray(), 0, sizeUploading);
                    outputStream.close();
                    request.connect();

                    if (request.getResponseCode() == 308) {
                        size = size + sizeUploading;
                        ByteArrayOutputStream temp = new ByteArrayOutputStream();
                        temp.write(chunk.toByteArray(), sizeUploading, (chunk.size() - sizeUploading));
                        chunk = temp;
//                        System.out.println("Chunked upload working: " + size);
                    } else if (request.getResponseCode() == 200 || request.getResponseCode() == 201) {
                        System.out.println("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
                    } else {
                        System.out.println("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
//                        System.out.println("last chunk Not working");
                    }
                }
            }catch (Exception e) {
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
                if(chunk.size() == 0)
                    request.setRequestProperty("Content-Range", "bytes */" + (size+chunk.size()));
                else
                    request.setRequestProperty("Content-Range", "bytes " + size + "-" + (size+chunk.size()-1) + "/" + (size + chunk.size()));
                request.setDoOutput(true);
                OutputStream outputStream = request.getOutputStream();
                outputStream.write(chunk.toByteArray(), 0, chunk.size());
                outputStream.close();
                request.connect();
                if(request.getResponseCode() == 200 || request.getResponseCode() == 201){
                    System.out.println("code: " + request.getResponseCode()+
                            ", message: "+ request.getResponseMessage());
                }else {
                    System.out.println("code: " + request.getResponseCode()+
                            ", message: "+ request.getResponseMessage());
                    System.out.println("fail");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public Mono<GoogleDriveResource> select(String path) {
        return null;
    }
}
