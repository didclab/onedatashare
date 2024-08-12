package org.onedatashare.server.module;

import org.apache.commons.vfs2.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.exceptionHandler.error.ODSException;
import org.onedatashare.server.model.filesystem.exceptions.FileAlreadyExistsException;
import org.onedatashare.server.model.filesystem.exceptions.FileNotFoundException;
import org.onedatashare.server.model.filesystem.exceptions.NoWritePermissionException;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

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
    public ResponseEntity delete(DeleteOperation operation) {
        try {
            FileObject fileObject = this.resolveFile(this.baseUri + operation.getPath() + operation.getToDelete());
            if(!fileObject.exists()){
                throw new FileNotFoundException();
            }
            if(fileObject.isWriteable()){
                fileObject.deleteAll();
                return new ResponseEntity(HttpStatus.OK);
            }
            else {
                throw new NoWritePermissionException();
            }
        } catch (FileSystemException | FileNotFoundException | NoWritePermissionException e) {
            throw new ODSException(e.getMessage(),e.getClass().getName());
        }
    }

    protected FileObject resolveFile(String path) throws FileSystemException {
        return this.fileSystemManager.resolveFile(path, this.fileSystemOptions);
    }

    private Stat fileToStat(FileObject file){
        Stat stat = new Stat();
        FileContent fileContent = getContent(file);
        FileType type = getType(file);
        if(type == FileType.FOLDER) {
            stat.setDir(true);
            stat.setFile(false);
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
    public Stat list(ListOperation listOperation) {
        try {
            Stat stat;
//                FileObject fileObject = this.resolveFile(this.baseUri + listOperation.getId());//this should be the path to the resource no the id of the resouce
            FileObject fileObject;
            if(listOperation.getPath().isEmpty() || listOperation.getPath() == null){
                logger.info("Listing", this.baseUri + "/"+ listOperation.getPath());
                fileObject = this.resolveFile(this.baseUri);
                logger.info(fileObject.toString());
            }else{
                logger.info("Listing", this.baseUri + "/"+ listOperation.getPath());
                fileObject = this.resolveFile(this.baseUri + "/" +listOperation.getPath());
                logger.info(fileObject.toString());
            }
            logger.info(fileObject.toString());
            if(!fileObject.exists()){
                throw new FileNotFoundException();
            }
            stat = fileToStat(fileObject);
            if(fileObject.getType() == FileType.FOLDER) {
                FileObject[] children = fileObject.getChildren();
                ArrayList<Stat> files = new ArrayList<>();
                for(FileObject file : children) {
                    files.add(fileToStat(file));
                }
                stat.setFiles(files);
            }
            return stat;
        } catch (FileSystemException | FileNotFoundException e) {
            throw new ODSException(e.getMessage(),e.getClass().getName());
        }
    }

    @Override
    public ResponseEntity mkdir(MkdirOperation mkdirOperation) {
        try {
            logger.info("Line number 160 VfsResource.java. Printing the baseUri + mkdirOperation.getPath() + mkdirOperation.getFolderToCreate()\n= {}", Paths.get(this.baseUri, mkdirOperation.getPath(), mkdirOperation.getFolderToCreate()).toString());
            logger.info(Paths.get(this.baseUri, mkdirOperation.getPath(), mkdirOperation.getFolderToCreate()).toString());
            FileObject fileObject = this.resolveFile(this.baseUri + "/"
                    + mkdirOperation.getPath() + mkdirOperation.getFolderToCreate());
            if(fileObject.exists()){
                throw new FileAlreadyExistsException();
            }
            if(fileObject.isWriteable()){
                throw new NoWritePermissionException();
            }
            fileObject.createFolder();
            return new ResponseEntity(HttpStatus.OK);
        } catch (FileSystemException | FileAlreadyExistsException | NoWritePermissionException e) {
            throw new ODSException(e.getMessage(),e.getClass().getName());
        }
    }

    @Override
    public String download(DownloadOperation operation) {
        try {
            String url = this.baseUri + operation.getId() + operation.fileToDownload;
            FileObject fileObject = this.resolveFile(url);
            if(!fileObject.exists()){
                throw new FileNotFoundException();
            }
            return url;
        } catch (FileSystemException | FileNotFoundException e) {
            throw new ODSException(e.getMessage(),e.getClass().getName());
        }
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
        }catch (IOException e){
            return 0l;
        }
    }

    private long lastModified(FileContent fileContent){
        try {
            return fileContent.getLastModifiedTime()/1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }
}
