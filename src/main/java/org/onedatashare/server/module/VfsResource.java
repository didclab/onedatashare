package org.onedatashare.server.module;

import org.apache.commons.vfs2.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import org.onedatashare.server.model.response.DownloadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    private Stat fileToStat(Element elem) throws MalformedURLException, IOException {
        Stat stat = new Stat();
        String href = elem.attr("abs:href");
        if (elem.text().endsWith("/")) { // folder
            stat.setDir(true);
            stat.setFile(false);
        } else { // file
            Element parent = elem.parent();
            if (parent != null) {
                Pattern pattern = Pattern.compile("(?<=\\s)\\d+(?=\\s*$)"); // regex to match the file size that appears at the end of the string in the html page
                Matcher matcher = pattern.matcher(parent.html());
                if (matcher.find()) {
                    stat.setSize(Long.parseLong(matcher.group()));
                }
            }
            stat.setFile(true);
            stat.setDir(false);
        }
        
        stat.setName(URI.create(href).getPath());
        stat.setId(href);
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
                Stat stat = null;
                String path = (listOperation.getPath().isEmpty() || listOperation.getPath() == null ||  listOperation.getPath().equals("/")) ? this.baseUri : listOperation.getPath();
                Document doc = fetchAndParseHtml(path);
                if (doc == null) {
                    s.error(new FileNotFoundException());
                    return;
                }

                stat = fileToStat(doc);
                if (path.endsWith("/")) { // folder
                    Elements links = doc.select("a");
                    ArrayList<Stat> files = new ArrayList<>();
                    for (Element link : links) {
                        files.add(fileToStat(link));
                    }
                    stat.setFiles(files);
                }
                s.success(stat);
            } catch (IOException | NumberFormatException e) {
                s.error(e);
            }
        });
    }

    protected Document fetchAndParseHtml(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            return httpClient.execute(httpGet, httpResponse -> {
                int statusCode = httpResponse.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return Jsoup.parse(httpResponse.getEntity().getContent(), null, url);
                } else {
                    throw new IOException("Failed to fetch HTML, status code: " + statusCode);
                }
            });
        }
    }


    @Override
    public Mono<Void> mkdir(MkdirOperation mkdirOperation) {
        return Mono.create(s -> {
            try {
                logger.info("Line number 160 VfsResource.java. Printing the baseUri + mkdirOperation.getPath() + mkdirOperation.getFolderToCreate()\n= {}", Paths.get(this.baseUri, mkdirOperation.getPath(), mkdirOperation.getFolderToCreate()).toString());
                logger.info(Paths.get(this.baseUri, mkdirOperation.getPath(), mkdirOperation.getFolderToCreate()).toString());
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
}
