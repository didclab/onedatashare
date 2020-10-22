/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.module.googledrive;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.IOUtils;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.service.ODSLoggerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Resource class that provides services for Google Drive endpoint.
 */
public class GDriveResource extends Resource<GDriveSession, GDriveResource> {

    public static final String ROOT_DIR_ID = "root";
    private Drive drive;

    public GDriveResource(Drive drive, String path, String id) {
        super(path, id);
        this.drive = drive;
    }

    public Mono<GDriveResource> mkdir() {
        return Mono.create(s -> {
            try {
                String[] currpath = getPath().split("/");
                for(int i =0; i<currpath.length; i++){
                    if(currpath[i].equals("")){
                        continue;
                    }
                    File fileMetadata = new File();
                    fileMetadata.setName(currpath[i]);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(getId()));
                    File file = drive.files().create(fileMetadata)
                            .setFields("id")
                            .execute();
                    setId(file.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
                s.error(e);
            }
            s.success(this);
        });
    }

    public String mkdir(String directoryTree[]){
        String curId = ROOT_DIR_ID;

        for(int i=1; i< directoryTree.length-1; i++){
            String exisitingID = folderExistsCheck(curId, directoryTree[i]);
            if(exisitingID == null){
                try {
                    File fileMetadata = new File();
                    fileMetadata.setName(directoryTree[i]);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(curId));
                    File file = drive.files().create(fileMetadata)
                            .setFields("id")
                            .execute();
                    curId = file.getId();

                } catch (IOException ioe) {
                    ODSLoggerService.logError("Exception encountered while creating directory tree", ioe);
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

            Drive.Files.List request = getSession().getService().files().list()
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
            ODSLoggerService.logError("Exception encountered while checking if folder " + directoryName +
                                        " exists in " + curId, ioe);
        }
        return null;
    }

    public Mono<GDriveResource> delete() {
       return Mono.create(s -> {
           try {
               drive.files().delete(getId()).execute();
               //setId( getSession().idMap.get(getSession().idMap.size() - 1).getId() );
               //if(getId() == null && getSession().idMap.size() ==1)
               //    setId(ROOT_DIR_ID);
           } catch (Exception e) {
               s.error(e);
           }
           s.success(this);
       });
    }

    public Mono<String> download(){
        String downloadUrl ="";
        try {
            downloadUrl = "https://drive.google.com/uc?id="+ getId() +"&export=download";

        }catch(Exception exp){
            ODSLoggerService.logError("Error encountered while generating shared link for " + getPath(), exp);
        }
        return Mono.just(downloadUrl);
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    public Stat onStat() {
        Drive.Files.List result ;
        Stat stat = new Stat();
        stat.setName(getPath());
        stat.setId(getId());

        try {
            if (getPath().equals("/")) {
                stat.setDir(true);
                result = drive.files().list()
                    .setOrderBy("name")
                    .setQ("trashed=false and 'root' in parents")
                    .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");

                if (result == null)
                    throw new NotFoundException();

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
                    File googleDriveFile = drive.files().get(getId())
                                                .setFields("id, name, kind, mimeType, size, modifiedTime")
                                                .execute();
                    if (googleDriveFile.getMimeType().equals("application/vnd.google-apps.folder")) {
                        stat.setDir(true);

                        String query = new StringBuilder().append("trashed=false and ")
                                                .append("'" + getId() + "'").append(" in parents").toString();
                        result = drive.files().list()
                                        .setOrderBy("name").setQ(query)
                                        .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");
                        if (result == null)
                            throw new NotFoundException();

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
                        directorySize = buildDirectoryTree(sub, getId(), "/");
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

            Drive.Files.List request = getSession().getService().files().list()
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
            ODSLoggerService.logError("Exception encountered while building directory tree", ioe);
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

    public GDrive tap() {
        GDrive gDriveTap = new GDrive();
        return gDriveTap;
    }

    class GDrive implements Tap {
        long size;
        Drive drive = getSession().getService();
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
                            httpRequestGet.getHeaders().setRange("bytes=" + state + "-" + (size - 1));
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

    public GDriveDrain sink() {
        return new GDriveDrain().start();
    }

    public GDriveDrain sink(Stat stat){
        return new GDriveDrain().start(getPath() + stat.getName());
    }

    class GDriveDrain implements Drain {
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        long size = 0;
        String resumableSessionURL;

        String drainPath = getPath();
        Boolean isDirTransfer = false;

        @Override
        public GDriveDrain start(String drainPath) {
            this.drainPath = drainPath;
            this.isDirTransfer = true;
            return start();
        }

        @Override
        public GDriveDrain start() {
            try{
                String name[] = drainPath.split("/");

                if(isDirTransfer)
                    setId(mkdir(name));
                else
                    setId( getSession().idMap.get(getSession().idMap.size()-1).getId() );

                URL url = new URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("POST");
                request.setDoInput(true);
                request.setDoOutput(true);
                String tokenStr = ((OAuthCredential)(getSession().getCredential())).getToken();
                request.setRequestProperty("Authorization", "Bearer " + tokenStr);
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body;
                if(getId() !=null){
                     body = "{\"name\": \"" + name[name.length-1] + "\", \"parents\": [\"" + getId() + "\"]}";
                }else{
                     body = "{\"name\": \"" + name[name.length-1] + "\"}";
                }

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

                // Google drive only supports 258KB (1 << 18) of data transfer per request
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
                    } else if (request.getResponseCode() == 200 || request.getResponseCode() == 201) {
                        ODSLoggerService.logDebug("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
                    } else {
                        ODSLoggerService.logDebug("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
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
    @Override
    public Mono<GDriveResource> select(String path) {
        return null;
    }
}
