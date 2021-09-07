package org.onedatashare.server.module;

import org.apache.commons.vfs2.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.exceptions.FileAlreadyExistsException;
import org.onedatashare.server.model.filesystem.exceptions.FileNotFoundException;
import org.onedatashare.server.model.filesystem.exceptions.NoWritePermissionException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.model.response.DownloadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static org.onedatashare.server.model.core.ODSConstants.MAX_FILES_TRANSFERRABLE;

public class VfsResource extends Resource {
    protected FileSystemManager fileSystemManager;
    protected FileSystemOptions fileSystemOptions;
    protected String baseUri;
    Logger logger = LoggerFactory.getLogger(VfsResource.class);

    public VfsResource(EndpointCredential credential){
        this.credential = credential;
        this.baseUri = ((AccountEndpointCredential) credential).getUri();
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(s ->{
            try {
                FileObject fileObject = this.resolveFile(this.baseUri + operation.getPath() + operation.getToDelete());
                if(!fileObject.exists()){
                    s.error(new FileNotFoundException());
                    return;
                }
                if(fileObject.isWriteable()){
                    fileObject.deleteAll();
                    s.success();
                }
                else {
                    s.error(new NoWritePermissionException());
                }
            } catch (FileSystemException e) {
                s.error(e);
            }
        });
    }

    protected FileObject resolveFile(String path) throws FileSystemException {
        return this.fileSystemManager.resolveFile(path, this.fileSystemOptions);
    }

    private Stat fileToStat(FileObject file){
        Stat stat = new Stat();
        FileContent fileContent = getContent(file);
        FileType type = getType(file);
        if(type == FileType.FOLDER) {
            stat.setDir(false);
            stat.setFile(true);
        }else if(type == FileType.FILE) {
            stat.setSize(size(fileContent));
            stat.setFile(true);
            stat.setDir(false);
        }
        stat.setId(file.getName().getPath());
        stat.setName(file.getName().getBaseName());
        stat.setTime(lastModified(fileContent));
        return stat;
    }

//    @Override
//    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
//        return Mono.create(s -> {
//            String basePath = source.getInfo().getId();
//            List<TransferJobRequest.EntityInfo> filesToTransferList = new LinkedList<>();
//            Stack<FileObject> traversalStack = new Stack<>();
//            try {
//                for(TransferJobRequest.EntityInfo e : source.getInfoList()){
//                    FileObject fObject = this.fileSystemManager.resolveFile(this.baseUri + "/" + basePath + e.getId(),
//                            this.fileSystemOptions);
//                    traversalStack.push(fObject);
//                }
//                for(int files = MAX_FILES_TRANSFERRABLE ; files > 0 && ! traversalStack.isEmpty(); --files){
//                    FileObject curr = traversalStack.pop();
//                    if(curr.getType() == FileType.FOLDER){
//                        for(FileObject f : curr.getChildren()) {
//                            traversalStack.add(f);
//                        }
//                        //Add empty folders as well
//                        if(curr.getChildren().length == 0){
//                            String filePath = curr.getPublicURIString().substring(basePath.length());
//                            TransferJobRequest.EntityInfo fileInfo = new TransferJobRequest.EntityInfo()
//                                    .setPath(filePath);
//                            filesToTransferList.add(fileInfo);
//                        }
//                    }else if(curr.getType() == FileType.FILE) {
//                        String filePath = curr.getPublicURIString().substring(basePath.length());
//                        TransferJobRequest.EntityInfo fileInfo = new TransferJobRequest.EntityInfo()
//                                .setPath(filePath)
//                                .setSize(curr.getContent().getSize());
//                        filesToTransferList.add(fileInfo);
//                    }
//                }
//            }catch (Exception e){
//                s.error(e);
//            }
//            s.success(filesToTransferList);
//            return;
//        });
//    }

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return Mono.create(s -> {
            try {
                Stat stat;
//                FileObject fileObject = this.resolveFile(this.baseUri + listOperation.getId());//this should be the path to the resource no the id of the resouce
                FileObject fileObject;
                if(listOperation.getPath().isEmpty() || listOperation.getPath() == null){
                    fileObject = this.resolveFile(this.baseUri);
                }else{
                    fileObject = this.resolveFile(this.baseUri + "/" +listOperation.getPath());
                }

                if(!fileObject.exists()){
                    s.error(new FileNotFoundException());
                    return;
                }
                stat = fileToStat(fileObject);
                if(fileObject.getType() == FileType.FOLDER) {
                    FileObject[] children = fileObject.getChildren();
                    ArrayList<Stat> files = new ArrayList<>();
                    for(FileObject file : children) {
                        files.add(fileToStat(file));
                        logger.info(file.toString());
                    }
                    stat.setFiles(files);
                }
                logger.info(stat.toString());
                s.success(stat);
            } catch (FileSystemException e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return Mono.create(s -> {
            try {
                FileObject fileObject = this.resolveFile(this.baseUri + "/"
                        + mkdirOperation.getPath() + mkdirOperation.getFolderToCreate());
                if(fileObject.exists()){
                    s.error(new FileAlreadyExistsException());
                    return;
                }
                if(fileObject.isWriteable()){
                    s.error(new NoWritePermissionException());
                    return;
                }
                fileObject.createFolder();
                s.success();
            } catch (FileSystemException e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono download(DownloadOperation operation) {
        return Mono.create(s -> {
            try {
                String url = this.baseUri + operation.getId() + operation.fileToDownload;
                FileObject fileObject = this.resolveFile(url);
                if(!fileObject.exists()){
                    s.error(new FileNotFoundException());
                    return;
                }
                s.success(new DownloadResponse(url));
            } catch (FileSystemException e) {
                s.error(e);
            }
        });
    }

    private FileContent getContent(FileObject file){
        try {
            return file.getContent();
        } catch (FileSystemException e) {
            e.printStackTrace();
            return null;
        }
    }
    private FileType getType(FileObject file){
        try {
            return file.getType();
        } catch (FileSystemException e) {
            e.printStackTrace();
            return null;
        }
    }

    private long size(FileContent fileContent){
        try{
            return fileContent.getSize();
        }catch (FileSystemException e){
            return 0l;
        }
    }

    private long lastModified(FileContent fileContent){
        try {
            return fileContent.getLastModifiedTime()/1000;
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return 0l;
    }
}
