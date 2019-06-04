package org.onedatashare.server.module.box;

import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Session;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.module.dropbox.DbxResource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BoxResource extends Resource<BoxSession, BoxResource> {

    protected BoxResource(BoxSession session, String path, String id) {
        super(session, path, id);
    }
    public BoxResource(BoxSession session, String path) {
        super(session, path);
    }


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
                    statChild.size = childFolder.getInfo().getSize();
                    statChild.time = childFolder.getInfo().getContentModifiedAt().getTime()/1000;
                }

            if(statChild.file){
                BoxFile childFile = new BoxFile(session.client, statChild.id);
                statChild.size = childFile.getInfo().getSize();
                statChild.time = childFile.getInfo().getContentModifiedAt().getTime()/1000;
            }

                //statChild.size = child.getSize();

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

    @Override
    public Mono<BoxResource> select(String path) {
        return session.select(path);
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }
}
