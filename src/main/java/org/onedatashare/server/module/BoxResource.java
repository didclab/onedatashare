package org.onedatashare.server.module;

import com.box.sdk.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.error.ODSAccessDeniedException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

public class BoxResource extends Resource {
    private BoxAPIConnection client;
    Logger logger = LoggerFactory.getLogger(BoxResource.class);

    public BoxResource(EndpointCredential credential) {
        super(credential);
        OAuthEndpointCredential oAuthEndpointCredential = (OAuthEndpointCredential) credential;
        logger.info(oAuthEndpointCredential.toString());
        this.client = new BoxAPIConnection(oAuthEndpointCredential.getToken());
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential) {
        return Mono.create(s -> {
            try {
                BoxResource boxResource = new BoxResource(credential);
                s.success(boxResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(s -> {
            BoxTrash boxTrash = new BoxTrash(this.client);
            try{
                boxTrash.deleteFile(operation.getToDelete());
                s.success();
            }catch (BoxAPIException boxAPIException){}
            try{
                boxTrash.deleteFolder(operation.getToDelete());
                s.success();
            }catch (BoxAPIException boxAPIResponseException){
                logger.error("Failed to delete the box id specified in the toDelete field");
                boxAPIResponseException.printStackTrace();
            }
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.create(s -> {
            try {
                Stat betterStat = new Stat();
                if (operation.getId().isEmpty() || operation.getId() == null) {
                    operation.setId("0");
                    betterStat.setName("Root");
                }
                betterStat.setFilesList(new ArrayList<>());
                BoxFolder folder = new BoxFolder(this.client, operation.getId()); //generally speaking u would only ever list a directory
                betterStat.setDir(true).setFile(false);
                betterStat.setId(folder.getID());
                betterStat.setName(folder.getInfo().getName());
                betterStat.setSize(folder.getInfo().getSize());
                ArrayList<Stat> childList = new ArrayList<>();
                for (BoxItem.Info itemInfo : folder) {
                    if (itemInfo instanceof BoxFile.Info) {
                        BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
                        childList.add(boxFileToStat(fileInfo));
                        // Do something with the file.
                    } else if (itemInfo instanceof BoxFolder.Info) {
                        BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
                        childList.add(boxFolderToStat(folderInfo));
                    }
                }
                betterStat.setFilesList(childList);
                s.success(betterStat);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return Mono.create(s -> {
            String[] pathToMake = operation.getFolderToCreate().split("/");
            BoxFolder parentFolder = null;
            String operationId = operation.getId();
            if (operation.getId() == null || operation.getId().isEmpty()) {
                parentFolder = BoxFolder.getRootFolder(this.client);
            } else {
                parentFolder = new BoxFolder(this.client, operationId);
            }
            for (String partOfPath : pathToMake) {
                BoxFolder.Info folder = parentFolder.createFolder(partOfPath);
                operationId = folder.getID();
                parentFolder = new BoxFolder(this.client, operationId);
            }
            s.success();
        });
    }

    /**
     * this is not needed as we do not offer downloading over browser anymore.
     * The reason is there is no optimization applied to the download then.
     * Leaving this incase we decide to change this
     *
     * @param operation
     * @return
     */
    @Override
    public Mono download(DownloadOperation operation) {
        return Mono.create(s -> {
            String url = "";
            try {
                BoxFile file = new BoxFile(this.client, operation.getId());
                url = file.getDownloadURL().toString();
                s.success(url);
            } catch (BoxAPIResponseException be) {
                if (be.getResponseCode() == 403) {
                    s.error(new ODSAccessDeniedException(403));
                }
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    public Stat boxFileToStat(BoxItem.Info fileInfo) {
        Stat stat = new Stat();
        stat.setId(fileInfo.getID());
        stat.setName(fileInfo.getName());
        stat.setSize(fileInfo.getSize());
        stat.setFile(true);
        stat.setDir(false);
        try {
            stat.setTime(fileInfo.getCreatedAt().getTime());
        } catch (NullPointerException ignored) {
        }
        return stat;
    }

    public Stat boxFolderToStat(BoxFolder.Info folderInfo) {
        Stat stat = new Stat();
        stat.setFiles(new ArrayList<>());
        stat.setId(folderInfo.getID());
        stat.setName(folderInfo.getName());
        stat.setDir(true);
        try {
            stat.setTime(folderInfo.getCreatedAt().getTime());
        } catch (NullPointerException ignored) {
        }
        stat.setFile(false);
        if (folderInfo.getCreatedAt() != null) {
            stat.setTime(folderInfo.getCreatedAt().getTime());
        }
        return stat;
    }
}
