package org.onedatashare.server.module;

import com.box.sdk.*;
import org.onedatashare.server.controller.EndpointCredController;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.error.ODSAccessDeniedException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BoxResource extends Resource {
    private BoxAPIConnection client;

    public BoxResource(EndpointCredential credential) {
        super(credential);
        this.client = new BoxAPIConnection(((OAuthEndpointCredential) credential).getToken());
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential) {
        return Mono.create(s -> {
            try {
                OAuthEndpointCredential oAuthEndpointCredential = (OAuthEndpointCredential) credential;
                BoxResource boxResource = new BoxResource(oAuthEndpointCredential);
                s.success(boxResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    public Stat onStat(String id) throws Exception {
        BoxFolder folder = null;
        BoxItem item;
        if (id == null) {
            folder = BoxFolder.getRootFolder(this.client);
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
        try {
            folder = new BoxFolder(this.client, id);
            item = folder;
            type = item.getInfo().getType();
        } catch (BoxAPIResponseException e) {
            item = new BoxFile(this.client, id);
            type = item.getInfo().getType();
        }

        if (type.equals("folder")) {
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
        } else {
            BoxFile file = new BoxFile(this.client, id);
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
     *
     * @param children Takes in an Iterable Object of type BoxItem.Info from the parent Box Folder
     * @return Stat object with a directory built
     * @author Javier Falca
     */
    public Stat buildDirStat(Iterable<BoxItem.Info> children) {
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
                BoxFolder childFolder = new BoxFolder(this.client, statChild.getId());
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
                BoxFile childFile = new BoxFile(this.client, statChild.getId());
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
        stat.setFiles(stat.getFiles());
        return stat;
    }


    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(s -> {
            try {
                if (onStat(operation.getId()).isFile()) {
                    BoxFile file = new BoxFile(this.client, operation.getId());
                    file.delete();
                } else if (onStat(operation.getId()).isDir()) {
                    BoxFolder folder = new BoxFolder(this.client, operation.getId());
                    folder.delete(true);
                }
                s.success();
            } catch (BoxAPIResponseException be) {
                if (be.getResponseCode() == 403) {
                    s.error(new ODSAccessDeniedException(403));
                }
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.create(s -> {
            try {
                Stat betterStat = new Stat();
                betterStat.setFilesList(new ArrayList<>());
                BoxFolder folder = new BoxFolder(this.client, operation.getId());
                betterStat.setId(folder.getID());
                betterStat.setName(folder.getID());
                betterStat.setPermissions(folder.getInfo().getPermissions().toString());
                betterStat.setSize(folder.getInfo().getSize());
                for (BoxItem.Info itemInfo : folder) {
                    if (itemInfo instanceof BoxFile.Info) {
                        BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
                        betterStat.getFilesList().add(boxFileToStat(fileInfo));
                        // Do something with the file.
                    } else if (itemInfo instanceof BoxFolder.Info) {
                        BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
                        betterStat.getFilesList().add(boxFolderToStat(folderInfo));
                    }
                }
                s.success(betterStat);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return Mono.create(s -> {
            try {
                String[] currpath = operation.getFolderToCreate().split("/");
                String folderId = operation.getId();
                if (folderId == null) {
                    folderId = "0";
                }
                for (String f : currpath) {
                    BoxFolder parentFolder = new BoxFolder(this.client, folderId);
                    BoxFolder.Info folder = parentFolder.createFolder(currpath[currpath.length - 1]);
                    folderId = folder.getID();
                }
                s.success();
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    /**
     * this is not needed as we do not offer downloading over browser anymore.
     * The reason is there is no optimization applied to the download then.
     * Leaving this incase we decide to change this
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

    public Stat boxFileToStat(BoxItem.Info fileInfo){
        Stat stat = new Stat();
        stat.setName(fileInfo.getName());
        stat.setSize(fileInfo.getSize());
        stat.setFile(true);
        stat.setDir(false);
        stat.setTime(fileInfo.getCreatedAt().getTime());
        return stat;
    }

    public Stat boxFolderToStat(BoxFolder.Info folderInfo){
        Stat stat = new Stat();
        stat.setFiles(new ArrayList<>());
        stat.setName(folderInfo.getName());
        stat.setDir(true);
        stat.setFile(false);
        stat.setTime(folderInfo.getCreatedAt().getTime());
        return stat;
    }
}
