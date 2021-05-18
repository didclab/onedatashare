package org.onedatashare.server.module;

import com.box.sdk.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.error.ODSAccessDeniedException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BoxResource extends Resource{

    private BoxAPIConnection client;

    public BoxResource(EndpointCredential credential){
        super(credential);
        this.client = new BoxAPIConnection(((OAuthEndpointCredential) credential).getToken());
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(s -> {
            try {
                OAuthEndpointCredential accountEndpointCredential = (OAuthEndpointCredential) credential;
                BoxResource boxResource = new BoxResource(accountEndpointCredential);
                s.success(boxResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    public Stat onStat(String id){
        BoxFolder folder = null;
        BoxItem item;
        if (id == null){
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
        try{
            folder = new BoxFolder(this.client, id);
            item = folder;
            type = item.getInfo().getType();
        } catch(BoxAPIResponseException e){
            item = new BoxFile(this.client, id);
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
        } else{
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
        stat.setFiles(contents.toArray(stat.getFiles()));
        return stat;
    }


    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(s ->{
            try{
                BoxFolder folder = new BoxFolder(this.client, operation.getToDelete());
                folder.delete(true);
                s.success();
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                BoxFile file = new BoxFile(this.client, operation.getToDelete());
                file.delete();
                s.success();
            }catch (Exception e){
                e.printStackTrace();
            }
            s.error(new ODSAccessDeniedException(403));
        });
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.create(s -> {
            try {
                Stat parent = new Stat();
                BoxFolder folder;
                if(operation.getId().isEmpty()){
                    folder = BoxFolder.getRootFolder(this.client);
                    parent.setName("root");
                }else{
                    folder = new BoxFolder(this.client, operation.getId());//the id is the id of the directory you are trying to access
                    parent.setName(operation.getId());
                }
                List<Stat> children = boxIterableToStat(folder.getChildren());
                parent.setFiles(children);
                s.success(parent);
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
                String parentId = operation.getId();
                if (parentId == null || parentId.isEmpty()) {
                    parentId = "0";
                }
                for(String f : currpath) {
                    BoxFolder parentFolder = new BoxFolder(this.client, parentId);
                    parentFolder.createFolder(f);
                    BoxFolder.Info folder = parentFolder.createFolder(currpath[currpath.length - 1]);
                    parentId = folder.getID();
                }
                s.success();
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    public List<Stat> boxIterableToStat(Iterable<BoxItem.Info> items){
        ArrayList<Stat> statList = new ArrayList<>();
        for(BoxItem.Info info : items){
            Stat stat = new Stat();
            stat.setName(info.getName()).setId(info.getID());
            if(info instanceof BoxFile.Info){
                stat.setFile(true).setDir(false);
            }else if(info instanceof BoxFolder.Info){
                stat.setFile(false).setDir(true);
            }
            stat.setSize(info.getSize());
            stat.setTime(info.getContentModifiedAt().getTime() / 1000);
            statList.add(stat);
        }
        return statList;
    }
}
