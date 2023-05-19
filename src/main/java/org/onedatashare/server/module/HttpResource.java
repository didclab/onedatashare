package org.onedatashare.server.module;

import lombok.SneakyThrows;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HttpResource extends VfsResource {

    AccountEndpointCredential credential;

    public HttpResource(EndpointCredential credential) {
        super(credential);
        this.fileSystemOptions = new FileSystemOptions();
        HttpFileSystemConfigBuilder.getInstance().setRootURI(this.fileSystemOptions, "/");
        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;

        if (accountCredential.getUsername() != null && accountCredential.getSecret() != null) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(accountCredential.getUri(), accountCredential.getUsername(), accountCredential.getSecret());
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, auth);
        }
    }

    @SneakyThrows
    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        Stat stat = new Stat();
        AccountEndpointCredential cred = (AccountEndpointCredential) super.credential;
        Path path;
        if(listOperation.getPath() == null){
             path = Paths.get(listOperation.getId());
        }else{
            path = Paths.get(listOperation.getPath());
        }
        Document doc = Jsoup.connect(cred.getUri() + path).get();

        Elements elements =  doc.select("body a");
        ArrayList<Stat> fileList = new ArrayList<>();
        for (Element elem : elements) {
            if(elem.text().equals("../") || elem.text().equals("./")) continue;
            if (elem.text().endsWith("/")) {
                fileList.add(this.folderFromElement(elem, path));
            } else {
                //we have a file
                fileList.add(this.fileFromElement(elem));
            }
        }
        stat.setFiles(fileList);
        return Mono.just(stat);
    }

    public Stat fileFromElement(Element elem) throws IOException {
        Stat fileInfo = new Stat();
        URL url = new URL(elem.absUrl("href"));
        long fileSize = url.openConnection().getContentLengthLong();
        Path path = Path.of(url.getPath());
        fileInfo.setName(elem.text());
        fileInfo.setSize(fileSize);
        fileInfo.setId(path.toAbsolutePath().toString());
        return fileInfo;
    }
    public Stat folderFromElement(Element elem, Path path) {
        Stat folderStat = new Stat();
        folderStat.setFile(false);
        folderStat.setDir(true);
        folderStat.setName(elem.text());
        folderStat.setId(path.toString());
        return folderStat;
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential) {
        return Mono.create(s -> {
            try {
                HttpResource httpResource = new HttpResource(credential);
                s.success(httpResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }
}
