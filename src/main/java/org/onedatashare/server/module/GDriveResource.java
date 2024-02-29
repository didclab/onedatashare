package org.onedatashare.server.module;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.exceptionHandler.error.NotFoundException;
import org.onedatashare.server.exceptionHandler.error.ODSException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.config.GDriveConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Resource class that provides services for Google Drive endpoint.
 */
public class GDriveResource extends Resource {
    public static GDriveConfig gDriveConfig;
    public static final String ROOT_DIR_ID = "root";
    private static final String DOWNLOAD_URL = "https://drive.google.com/uc?id=%s&export=download";
    private OAuthEndpointCredential credential;
    private Drive service;

    public GDriveResource(EndpointCredential credential) throws IOException {
        this.credential = (OAuthEndpointCredential) credential;
        gDriveConfig = GDriveConfig.getInstance();
        service = gDriveConfig.getDriveService(this.credential);

    }

    public Stat statHelper(String path, String id) throws IOException {
        Drive.Files.List result;
        Stat stat = new Stat()
                .setName(path)
                .setId(id);

        if (path.equals("/") || id.equals("/") || path.isEmpty() || id.isEmpty() || id.equals("0") ||path.equals("0")) {
            stat.setDir(true);
            result = this.service.files().list()
                    .setOrderBy("name")
                    .setQ("trashed=false and 'root' in parents")
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, kind, mimeType, size, modifiedTime)");
            if (result == null) {
                throw new NotFoundException();
            }

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
            File googleDriveFile = this.service.files().get(id)
                    .setFields("id, name, kind, mimeType, size, modifiedTime")
                    .execute();
            if (googleDriveFile.getMimeType().equals("application/vnd.google-apps.folder")) {
                stat.setDir(true);

                String query = new StringBuilder().append("trashed=false and ")
                        .append("'" + id + "'").append(" in parents").toString();
                result = this.service.files().list()
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
        }

        return stat;
    }

    private Stat mDataToStat(File file) {
        Stat stat = new Stat(file.getName());

        try {
            stat.setFile(true);
            stat.setId(file.getId());
            stat.setName(file.getName());
            stat.setTime(file.getModifiedTime().getValue()/1000);
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                stat.setDir(true).setFile(false);
            } else if(file.containsKey("size")){
                stat.setSize(file.getSize()).setDir(false);
            }
        }
        catch (NullPointerException  e) {

        }catch (Exception  e) {
            e.printStackTrace();
        }
        return stat;
    }

    @Override
    public ResponseEntity delete(DeleteOperation operation) throws IOException{
            this.service.files().delete(operation.getToDelete()).execute();
            return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public Stat list(ListOperation operation) throws IOException{
            return statHelper(operation.getPath(), operation.getId());

    }

    @Override
    public ResponseEntity mkdir(MkdirOperation operation) throws IOException{
//                if(operation.getId() == null || operation.getId().equals("/") || operation.getId().equals("0")){
//                    operation.setId("drive");
//                }
        String[] foldersToCreate = operation.getFolderToCreate().split("/");
        String currId = operation.getId();
        for(int i =0; i < foldersToCreate.length; i++){
            if(foldersToCreate[i].equals("")){
                continue;
            }
            File fileMetadata = new File();
            fileMetadata.setName(foldersToCreate[i]);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if(operation.getId() != null && !operation.getId().isEmpty()){
                fileMetadata.setParents(Collections.singletonList(currId));
            }
            File file = this.service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            currId = file.getId();
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    public String download(DownloadOperation operation) {
        return null;
    }


    public static Resource initialize(EndpointCredential credential){
        try {
            GDriveResource gDriveResource= new GDriveResource(credential);
            return gDriveResource;
        } catch (Exception e) {
            throw new ODSException(e.getMessage(),e.getClass().getName());
        }
    }
}