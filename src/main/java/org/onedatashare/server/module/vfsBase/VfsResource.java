package org.onedatashare.server.module.vfsBase;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.resource.Resource;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static org.onedatashare.server.model.core.ODSConstants.MAX_FILES_TRANSFERRABLE;

public class VfsResource extends Resource {
    protected FileSystemManager fileSystemManager;
    protected FileSystemOptions fileSystemOptions;

    public VfsResource(EndpointCredential credential){
        this.credential = credential;
    }
    

    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return Mono.create(s -> {
            String basePath = source.getInfo().getPath();
            List<TransferJobRequest.EntityInfo> filesToTransferList = new LinkedList<>();
            Stack<FileObject> traversalStack = new Stack<>();
            try {
                for(TransferJobRequest.EntityInfo e : source.getInfoList()){
                    FileObject fObject = this.fileSystemManager.resolveFile(basePath + e.getPath(), this.fileSystemOptions);
                    traversalStack.push(fObject);
                }
                for(int files = MAX_FILES_TRANSFERRABLE ; files > 0 && ! traversalStack.isEmpty(); --files){
                    FileObject curr = traversalStack.pop();
                    if(curr.getType() == FileType.FOLDER){
                        for(FileObject f : curr.getChildren()) {
                            traversalStack.add(f);
                        }
                        //Add empty folders as well
                        if(curr.getChildren().length == 0){
                            String filePath = curr.getPublicURIString().substring(basePath.length());
                            TransferJobRequest.EntityInfo fileInfo = new TransferJobRequest.EntityInfo()
                                    .setPath(filePath);
                            filesToTransferList.add(fileInfo);
                        }
                    }else if(curr.getType() == FileType.FILE) {
                        String filePath = curr.getPublicURIString().substring(basePath.length());
                        TransferJobRequest.EntityInfo fileInfo = new TransferJobRequest.EntityInfo()
                                .setPath(filePath)
                                .setSize(curr.getContent().getSize());
                        filesToTransferList.add(fileInfo);
                    }
                }
            }catch (Exception e){
                s.error(e);
            }
            s.success(filesToTransferList);
            return;
        });
    }
}
