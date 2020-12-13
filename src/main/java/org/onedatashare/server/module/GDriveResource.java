package org.onedatashare.server.module;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.googledrive.GDriveConfig;
import org.onedatashare.server.service.ODSLoggerService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Resource class that provides services for Google Drive endpoint.
 */
public class GDriveResource extends Resource {
    public static GDriveConfig gDriveConfig = new GDriveConfig();
    public static final String ROOT_DIR_ID = "root";
    private static final String DOWNLOAD_URL = "https://drive.google.com/uc?id=%s&export=download";

    private Drive service;

    public GDriveResource(EndpointCredential credential) throws IOException {
        this.credential = credential;
        this.service = gDriveConfig.getDriveService((OAuthEndpointCredential) credential);
    }

    public String folderExistsCheck(String curId, String directoryName){
        try {
            String query = new StringBuilder().append("trashed=false and ").append("'" + curId + "'")
                    .append(" in parents").toString();

            Drive.Files.List request = this.service.files().list()
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

    public Stat statHelper(String path, String id) throws IOException {
        Drive.Files.List result;
        Stat stat = new Stat()
                .setName(path)
                .setId(id);

        if (path.equals("/")) {
            stat.setDir(true);
            result = this.service.files().list()
                    .setOrderBy("name")
                    .setQ("trashed=false and 'root' in parents")
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

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(s -> {
            try {
                this.service.files().delete(operation.getId()).execute();
                s.success();
            } catch (IOException e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.create(s ->{
            try {
                Stat stat = statHelper(operation.getPath(), operation.getId());
                s.success(stat);
            } catch (IOException e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return Mono.create(s -> {
            try {
                String[] foldersToCreate = operation.getFolderToCreate().split("/");
                String currId = operation.getId();
                for(int i =0; i < foldersToCreate.length; i++){
                    if(foldersToCreate[i].equals("")){
                        continue;
                    }
                    File fileMetadata = new File();
                    fileMetadata.setName(foldersToCreate[i]);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(currId));
                    File file = this.service.files().create(fileMetadata)
                            .setFields("id")
                            .execute();
                    currId = file.getId();
                }
            } catch (IOException e) {
                s.error(e);
            }
            s.success();
        });
    }

    @Override
    public Mono download(DownloadOperation operation) {
        return Mono.just(String.format(DOWNLOAD_URL, operation.getId()));
    }

    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return null;
    }
}