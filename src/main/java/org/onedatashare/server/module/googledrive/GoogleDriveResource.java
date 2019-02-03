package org.onedatashare.server.module.googledrive;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.IOUtils;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.NotFound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GoogleDriveResource extends Resource<GoogleDriveSession, GoogleDriveResource> {

    protected GoogleDriveResource(GoogleDriveSession session, String path, String id) {
        super(session, path, id);
    }
    protected GoogleDriveResource(GoogleDriveSession session, String path) {
        super(session, path,null);
    }

    public Mono<GoogleDriveResource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try {
                String[] currpath = path.split("/");

                for(int i =0; i<currpath.length; i++){
                    System.out.println("Parent ID: " + id);
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
            }
        });
    }

   public Mono<GoogleDriveResource> delete() {
       return initialize().map(resource -> {
           try {
               resource.session.service.files().delete(id).execute();
               id = session.idMap.get(session.idMap.size()-1).getId();
               System.out.println(id+"**"+session.idMap.get(session.idMap.size()-1).getPath());
           } catch (Exception e) {
               e.printStackTrace();
           }
           return resource;
       });
   }

    public Mono<Stat> stat() {
        return initialize().map(GoogleDriveResource::onStat);
    }

    public Stat onStat() {
        Drive.Files.List result ;
        Stat stat = new Stat();
        stat.name = path;
        stat.id = id;

        try {
            if (path.equals("/")) {
                stat.dir = true;
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
                        fileSet.setNextPageToken(null);
                        e.printStackTrace();
                    }
                }
                while (result.getPageToken() != null);
            } else {
                try {
                    File googleDriveFile = session.service.files().get(id)
                            .setFields("id, name, kind, mimeType, size, modifiedTime").execute();
                    if (googleDriveFile.getMimeType().equals("application/vnd.google-apps.folder")) {
                        stat.dir = true;

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
                        stat.file = true;
                        stat.time = googleDriveFile.getModifiedTime().getValue() / 1000;
                        stat.size = googleDriveFile.getSize();
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

    private Stat mDataToStat(File file) {
        Stat stat = new Stat(file.getName());

        try {
            stat.file = true;
            stat.id = file.getId();
            stat.time = file.getModifiedTime().getValue()/1000;
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                stat.dir = true;
                stat.file = false;
            }
            else
                stat.size = file.getSize();
        }
        catch (Exception  e) {
          e.printStackTrace();
        }

        return stat;
    }

    public GoogleDriveTap tap() {
        return new GoogleDriveTap();
    }

    class GoogleDriveTap implements Tap {
        Drive.Files.Get downloadBuilder;
        final long size = stat().block().size;
        Drive drive = session.service;

        String downloadUrl = "https://www.googleapis.com/drive/v3/files/"+id+"?alt=media";
        com.google.api.client.http.HttpRequest httpRequestGet;
        {
            try {
                httpRequestGet = drive.getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl));
                downloadBuilder = session.service.files().get(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                                IOUtils.copy((InputStream)is, (OutputStream)outputStream);
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
                                IOUtils.copy((InputStream)is, (OutputStream)outputStream);
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

    class GoogleDriveDrain implements Drain {
        long bytesWritten = 0;
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        long size = 0;
        String resumableSessionURL;
        String upload_id;

        @Override
        public GoogleDriveDrain start() {
            try{
                String parentid = session.idMap.get(session.idMap.size()-1).getId();
                if( parentid != null ) {
                    id = session.idMap.get(session.idMap.size()-1).getId();
                    System.out.println("File: " + path+" Id: "+id);
                }else {
                    //System.out.println(stat.name + "has no parent");
                    System.out.println(path);
                    System.out.println(session.pathToParentIdMap.toString());
                }
                //size = stat.size;
                String name[] = path.split("/");

                URL url = new URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("POST");
                request.setRequestProperty("Authorization", "Bearer " + ((OAuthCredential)(session.getCredential())).token);
                //request.setRequestProperty("X-Upload-Content-Type", "application/pdf");
                //request.setRequestProperty("X-Upload-Content-Length", Long.toString(size));
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body = "{\"name\": \"" + name[name.length-1] + "\", \"parents\": [\"" + id + "\"]}";
                System.out.println("name:"+name[name.length-1]);
                request.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
                request.setDoOutput(true);
                OutputStream outputStream = request.getOutputStream();
                outputStream.write(body.getBytes());
                outputStream.close();
                request.connect();
                if(request.getResponseCode() == 200) {
                    resumableSessionURL = request.getHeaderField("location");
                    /*String location = request.getHeaderField("Location");
                    if (location.contains("upload_id")) {
                        String[] uploadParameters = location.split("upload_id");
                        upload_id = uploadParameters[1].replace("=", "");
                    }*/
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
                if(sizeUploading > 0) {
                    URL url = new URL(resumableSessionURL);
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
                        System.out.println("Chunked upload working: " + size);
                    } else if (request.getResponseCode() == 200 || request.getResponseCode() == 201) {
                        System.out.println("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
                    } else {
                        System.out.println("code: " + request.getResponseCode() +
                                ", message: " + request.getResponseMessage());
                        System.out.println("last chunk Not working");
                    }

                }

               /* long chunkSize = chunk.size();
                //size = 1257197;//slice.length();
                System.out.println("chunk: "+chunkSize+" slice: "+slice.length());

                byte[] chunkContents = slice.asBytes();
                URL url = new URL(resumableSessionURL);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("PUT");
                request.setConnectTimeout(10000);
                //request.setRequestProperty("Content-Type", "application/pdf");
                request.setRequestProperty("Content-Length", Long.toString(slice.length()));
                *//*if(chunkSize % slice.length() ==0){
                    request.setRequestProperty("Content-Range", "bytes " + bytesWritten + "-" + (bytesWritten + slice.length()-1) + "/" + "*");
                }else{
                    request.setRequestProperty("Content-Range", "bytes " + bytesWritten + "-" + (bytesWritten + slice.length()-1) + "/" + chunkSize);
                }*//*
                request.setRequestProperty("Content-Range", "bytes " + bytesWritten + "-" + (bytesWritten + slice.length()-1) + "/" + "*");
                request.setDoOutput(true);
                OutputStream outputStream = request.getOutputStream();
                outputStream.write(chunkContents, 0, (int)slice.length());
                outputStream.close();
                request.connect();

                //chunk = new ByteArrayOutputStream();
                //chunk.write(chunkContents, 262144, chunkContents.length - 262144);

                if(request.getResponseCode() == 308) {
                    String range = request.getHeaderField("range");
                    bytesWritten = Long.parseLong(range.substring(range.lastIndexOf("-") + 1)) + 1;
                    System.out.println("Chunked upload working: "+bytesWritten);
                }else if(request.getResponseCode() == 200 || request.getResponseCode() == 201){
                    System.out.println("code: " + request.getResponseCode()+
                            ", message: "+ request.getResponseMessage());
                }else {
                    System.out.println("code: " + request.getResponseCode()+
                            ", message: "+ request.getResponseMessage());
                    System.out.println("last chunk Not working");
                }*/

               /* if(size <= chunkSize*//*262144*//*) {
                    if(chunkSize == size) {
                        URL url = new URL(resumableSessionURL);
                        HttpURLConnection request = (HttpURLConnection) url.openConnection();
                        request.setRequestMethod("PUT");
                        request.setConnectTimeout(10000);
                        //request.setRequestProperty("Content-Type", "application/pdf");
                        request.setRequestProperty("Content-Length", Integer.toString(chunk.size()));
                        request.setRequestProperty("Content-Range", "bytes " + "0" + "-" + (size - 1) + "/" + size);
                        request.setDoOutput(true);
                        OutputStream outputStream = request.getOutputStream();
                        outputStream.write(chunk.toByteArray());
                        outputStream.close();
                        request.connect();

                        if(request.getResponseCode() == 308) {
                            System.out.println("Less than 256 not working properly");
                        }else if(request.getResponseCode() == 200 || request.getResponseCode() == 201){
                            System.out.println("Less than 256 working");
                        }else {
                            System.out.println("Less than 256 Not working");
                        }
                    }
                }else {
                    if(chunkSize >= chunkSize) {
                        byte[] chunkContents = chunk.toByteArray();
                        URL url = new URL(resumableSessionURL);
                        HttpURLConnection request = (HttpURLConnection) url.openConnection();
                        request.setRequestMethod("PUT");
                        request.setConnectTimeout(10000);
                        //request.setRequestProperty("Content-Type", "application/pdf");
                        request.setRequestProperty("Content-Length", Long.toString(chunkSize));
                        request.setRequestProperty("Content-Range", "bytes " + bytesWritten + "-" + (bytesWritten + chunkSize-1) + "/" + size);
                        request.setDoOutput(true);
                        OutputStream outputStream = request.getOutputStream();
                        outputStream.write(chunkContents, 0, (int)chunkSize);
                        outputStream.close();
                        request.connect();

                        //chunk = new ByteArrayOutputStream();
                        //chunk.write(chunkContents, 262144, chunkContents.length - 262144);

                        if(request.getResponseCode() == 308) {
                            String range = request.getHeaderField("range");
                            bytesWritten = Long.parseLong(range.substring(range.lastIndexOf("-") + 1)) + 1;
                            System.out.println("Chunked upload working: "+bytesWritten);
                        }else {
                            System.out.println("Unable to perform resumable uploads to google drive"+
                                    ", code: " + request.getResponseCode()+
                                    ", message: "+ request.getResponseMessage()+
                                    ", content: "+request);
                        }
                    }else if(chunk.size() + bytesWritten == size) {
                        byte[] chunkContents = chunk.toByteArray();
                        URL url = new URL(resumableSessionURL);
                        HttpURLConnection request = (HttpURLConnection) url.openConnection();
                        request.setRequestMethod("PUT");
                        request.setConnectTimeout(10000);
                        //request.setRequestProperty("Content-Type", "application/pdf");
                        request.setRequestProperty("Content-Length", Integer.toString(chunk.size()));
                        request.setRequestProperty("Content-Range", "bytes " + bytesWritten + "-" + (size - 1) + "/" + size);
                        request.setDoOutput(true);
                        OutputStream outputStream = request.getOutputStream();
                        outputStream.write(chunkContents, 0, chunk.size());
                        outputStream.close();
                        request.connect();
                        System.out.println("last Chunk upload: "+(chunk.size() + bytesWritten));
                        if(request.getResponseCode() == 308) {
                            System.out.println("last chunk not working properly");
                        }else if(request.getResponseCode() == 200 || request.getResponseCode() == 201){
                            System.out.println("last chunk working");
                        }else {
                            System.out.println("last chunk Not working");
                        }
                    }
                }*/
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
