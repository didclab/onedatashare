package org.onedatashare.server.module.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;

public class HttpResource extends Resource<HttpSession, HttpResource> {
    private String uri = null;

    protected HttpResource(HttpSession session, String uri) {
        super(session);
        this.uri = uri;
    }

    @Override
    public Mono<HttpResource> select(String path) {
        return null;
    }

    public Mono<Stat> stat() {
        return Mono.just(onStat());
    }

    public Stat onStat() {
        Stat stat = new Stat();
        URI uriType = URI.create(uri);

        stat.name = uriType.getHost();

        ArrayList<Stat> contents = null;
        try {
            Document document;
            document = Jsoup.connect(uri).get();
            Elements links = document.select("a[href]");
            contents = new ArrayList<Stat>(links.size());
            String fileName;
            Stat contentStat = null;
            for (Element link : links) {
                contentStat = new Stat();
                fileName = link.text();
                if (fileName.endsWith("/")) {
                    contentStat.name = fileName.substring(0, fileName.length() - 1);
                    contentStat.dir = true;
                    stat.file = false;
                } else {
                    contentStat.name = fileName;
                    contentStat.file = true;
                    contentStat.dir = false;
                }
                contents.add(contentStat);
            }
            stat.setFiles(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stat;
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }
}
