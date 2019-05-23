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

        // Get the hostname from the uri
        stat.name = uriType.toString();

        Document document = null;

        try {
            document = Jsoup.connect(uri).get();
            document.select("th").remove();
        } catch (IOException e) {
            System.out.println("Unable to fetch HTML file");
            return stat;
        }

        Elements table = document.select("tr");
        //Remove the header and 1st row of the table
        if(table.size() >= 2) {
            table.remove(1);
            table.remove(0);
        }

        Stat contentStat;
        ArrayList<Stat> contents = new ArrayList<>(table.size());

        for (Element row : table) {
            try {
                Elements rowContent = row.select("td");
                if (rowContent.size() == 0)
                    continue;
                contentStat = new Stat();
                String fileName = rowContent.get(1).text();
                String date = rowContent.get(2).text();
                if (fileName.endsWith("/")) {
                    contentStat.name = fileName.substring(0, fileName.length() - 1);
                    contentStat.dir = true;
                    contentStat.file = false;
                } else {
                    contentStat.name = fileName;
                    contentStat.dir = false;
                    contentStat.file = true;
                    contentStat.size = SizeParser.getBytes(rowContent.get(3).text());
//                    contentStat.time = null;
                }
                contents.add(contentStat);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        stat.setFiles(contents);
        return stat;
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }

    private static class SizeParser {

        public static long getBytes(String sizeString) {

            String multiplier = sizeString.replaceAll("\\D+","");
            String digits = sizeString.replaceAll("[^\\d]","");
            float size = Float.parseFloat(digits);

            final long K_FACTOR = 1024;
            final long M_FACTOR = 1024 * K_FACTOR;
            final long G_FACTOR = 1024 * M_FACTOR;

            switch (multiplier) {
                case "G":
                    size *= G_FACTOR;
                    break;
                case "M":
                    size *= M_FACTOR;
                    break;
                case "K":
                    size *= K_FACTOR;
                    break;
            }
            return Math.round(size);
        }
    }
}
