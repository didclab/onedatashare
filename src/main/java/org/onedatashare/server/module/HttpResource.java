package org.onedatashare.server.module;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;

import java.io.FileNotFoundException;
import java.io.IOException;


import reactor.core.publisher.Mono;

public class HttpResource extends Resource {

    private AccountEndpointCredential accountCredential;

    public HttpResource(EndpointCredential credential) throws FileSystemException {
        super(credential);
        this.accountCredential = (AccountEndpointCredential) credential;
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential){
        return Mono.create(s -> {
            try {
                HttpResource httpResource = new HttpResource(credential);
                s.success(httpResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.empty();
    }

    @Override
    public Mono<Stat> list(ListOperation listOperation) {
        return Mono.create(s -> {
            try {
                Stat stat = null;
                String path;
                if (listOperation.getPath().isEmpty() || listOperation.getPath() == null || listOperation.getPath().equals("/")) {
                    path = this.accountCredential.getUri();
                } else {
                    path = this.accountCredential.getUri() + "/" + listOperation.getPath();
                }
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

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return Mono.empty();
    }

    @Override
    public Mono download(DownloadOperation operation) {
        return Mono.empty();
    }
}

