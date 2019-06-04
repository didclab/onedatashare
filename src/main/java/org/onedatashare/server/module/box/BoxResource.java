package org.onedatashare.server.module.box;

import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSharedLink;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.EnumSet;

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

        Stat stat = new Stat();
        BoxFolder folder = new BoxFolder(session.client, id);
        if (id == null){
            folder = new BoxFolder(session.client, "0");
        }

        ArrayList<Stat> contents = new ArrayList<>();
        Iterable<BoxItem.Info> children = folder.getChildren();
        for(BoxItem.Info child : children) {

                Stat statChild = new Stat();
                statChild.file = child instanceof BoxFile.Info;
                statChild.dir = child instanceof BoxFolder.Info;
                statChild.id = child.getID();
                statChild.name = child.getName();
                if(statChild.dir){
                    BoxFolder childFolder = new BoxFolder(session.client, statChild.id);
                    BoxFolder.Info childFolderInfo = childFolder.getInfo();
                    statChild.size = childFolderInfo.getSize();
                    statChild.time = childFolderInfo.getContentModifiedAt().getTime()/1000;
                    BoxSharedLink sharedLink = childFolderInfo.getSharedLink();
                    if(sharedLink != null) {
                        statChild.link = sharedLink.toString();
                    }
                    EnumSet<BoxFolder.Permission> permissions = childFolderInfo.getPermissions();
                    if(permissions != null){
                        statChild.perm = permissions.toString();
                    }

                }

            if(statChild.file){
                BoxFile childFile = new BoxFile(session.client, statChild.id);
                BoxFile.Info childFileInfo = childFile.getInfo();
                statChild.size = childFileInfo.getSize();
                statChild.time = childFileInfo.getContentModifiedAt().getTime()/1000;
                BoxSharedLink sharedLink = childFileInfo.getSharedLink();
                if(sharedLink != null) {
                    statChild.link = sharedLink.toString();
                }
                EnumSet<BoxFile.Permission> permissions = childFileInfo.getPermissions();
                if(permissions != null){
                    statChild.perm = permissions.toString();
                }
            }

                contents.add(statChild);
        }
        stat.files = new Stat[contents.size()];
        stat.files = contents.toArray(stat.files);

        return stat;
    }

    public Mono<BoxResource> mkdir() {
        return initialize().doOnSuccess(resource -> {
            try {
                String[] currpath = path.split("/");
                String folderId = id;
                if(id == null){
                 folderId = "0";
                }
                BoxFolder parentFolder = new BoxFolder(resource.session.client, folderId);
                BoxFolder.Info childFolder = parentFolder.createFolder(currpath[currpath.length-1]);

                id = childFolder.getID();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Mono<BoxResource> delete() {
        return initialize().doOnSuccess(resource -> {
            try {
                if(onStat().isFile()) {
                    BoxFile file = new BoxFile(resource.session.client, id);
                    file.delete();
                }
                else if(onStat().isDir()){
                    boolean recursive = true;
                    BoxFolder folder = new BoxFolder(resource.session.client, id);
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
            BoxFile file = new BoxFile(session.client, id);
            url = file.getDownloadURL().toString();

        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.just(url);
    }

    @Override
    public Mono<BoxResource> select(String path) {
        return session.select(path);
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }
}
