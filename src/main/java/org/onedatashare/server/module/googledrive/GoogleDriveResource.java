package org.onedatashare.server.module.googledrive;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.IOUtils;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.util.Util;
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

    public Mono<GoogleDriveResource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try {
                String currpath = session.mkdirQueue.take();
                if(session.pathToParentIdMap.isEmpty()) {
                    session.pathToParentIdMap.put(Util.up(currpath), id);
                }else {
                    id = session.pathToParentIdMap.get(Util.up(currpath));
                }

                File fileMetadata = new File();
                fileMetadata.setName(currpath);
                fileMetadata.setMimeType("application/vnd.google-apps.folder");
                fileMetadata.setParents(Collections.singletonList(id));

                File file = session.service.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                System.out.println("Folder ID: " + file.getId());
                id = file.getId();

                if(!session.pathToParentIdMap.isEmpty()) {
                    session.pathToParentIdMap.put(currpath, id);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }

   /* public Mono<GoogleDriveResource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try {
                String currpath = session.mkdirQueue.take();
                if(session.pathToParentIdMap.isEmpty()) {
                    session.pathToParentIdMap.put(currpath.up().toString(), id);
                }else {
                    id = session.pathToParentIdMap.get(currpath.up().toString());
                }

                File fileMetadata = new File();
                fileMetadata.setName(currpath.name());
                fileMetadata.setMimeType("application/vnd.google-apps.folder");
                fileMetadata.setParents(Collections.singletonList(id));

                File file = session.service.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
//        path.
                System.out.println("Folder ID: " + file.getId());
                id = file.getId();

                if(!session.pathToParentIdMap.isEmpty()) {
                    session.pathToParentIdMap.put(currpath.toString(), id);
                }

            }catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }
*/

    public Mono<Stat> stat() {
        return initialize().map(GoogleDriveResource::onStat);
    }

    public Stat onStat() {
        Drive.Files.List result ;
        Stat stat = new Stat();
        stat.name = path;
        stat.id = id;
        try {
            if (path == "/") {
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

                    } catch (Exception e) {
                        fileSet.setNextPageToken(null);
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
                        //System.out.println("A file");
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
    public GoogleDriveDrain sink() {
        return new GoogleDriveDrain().start();
    }

    class GoogleDriveTap implements Tap {
        Drive.Files.Get downloadBuilder;
        final long size = stat().block().size;
        Drive drive = session.service;
        String downloadUrl = path;
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
                        //byte[] outputStream = null;
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

    class GoogleDriveDrain implements Drain {
        long bytesWritten = 0;
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        long size;
        String resumableSessionURL;
        Stat stat = onStat();
        @Override
        public GoogleDriveDrain start() {
            try{
                String parentid = session.pathToParentIdMap.get(path);
                if( parentid != null ) {
                    id = session.pathToParentIdMap.get(path);
                    System.out.println("File: " + path);
                }else {
                    System.out.println(stat.name + "has no parent");
                    System.out.println(path);
                    System.out.println(session.pathToParentIdMap.toString());
                }
                size = stat.size;
                URL url = new URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("POST");
                request.setRequestProperty("Authorization", "Bearer " + ((OAuthCredential)(session.getCredential())).token);
                //request.setRequestProperty("X-Upload-Content-Type", "application/pdf");
                request.setRequestProperty("X-Upload-Content-Length", Long.toString(size));
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body = "{\"name\": \"" + stat.name + "\", \"parents\": [\"" + id + "\"]}";
                request.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
                request.setDoOutput(true);
                OutputStream outputStream = request.getOutputStream();
                outputStream.write(body.getBytes());
                outputStream.close();
                request.connect();
                if(request.getResponseCode() == 200) {
                    resumableSessionURL = request.getHeaderField("location");
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
                long chunkSize = chunk.size();
                if(size <= 262144) {
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
                    if(chunkSize >= 262144) {
                        byte[] chunkContents = chunk.toByteArray();
                        URL url = new URL(resumableSessionURL);
                        HttpURLConnection request = (HttpURLConnection) url.openConnection();
                        request.setRequestMethod("PUT");
                        request.setConnectTimeout(10000);
                        //request.setRequestProperty("Content-Type", "application/pdf");
                        request.setRequestProperty("Content-Length", Integer.toString(262144));
                        request.setRequestProperty("Content-Range", "bytes " + bytesWritten + "-" + (bytesWritten + 262143) + "/" + size);
                        request.setDoOutput(true);
                        OutputStream outputStream = request.getOutputStream();
                        outputStream.write(chunkContents, 0, 262144);
                        outputStream.close();
                        request.connect();

                        chunk = new ByteArrayOutputStream();
                        chunk.write(chunkContents, 262144, chunkContents.length - 262144);

                        if(request.getResponseCode() == 308) {
                            System.out.println("Chunked upload working");
                            String range = request.getHeaderField("range");
                            bytesWritten = Long.parseLong(range.substring(range.lastIndexOf("-") + 1)) + 1;
                        }else {
                            System.out.println("Unable to perform resumable uploads to google drive");
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

                        if(request.getResponseCode() == 308) {
                            System.out.println("last chunk not working properly");
                        }else if(request.getResponseCode() == 200 || request.getResponseCode() == 201){
                            System.out.println("last chunk working");
                        }else {
                            System.out.println("last chunk Not working");
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void finish() {
            try{

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
