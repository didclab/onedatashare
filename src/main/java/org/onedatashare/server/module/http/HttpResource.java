package org.onedatashare.server.module.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Stat;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpResource extends Resource<HttpSession, HttpResource> {
    private String uri;

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

        // Get the hostname from the uri
        stat.name = URI.create(uri).toString();

        Document document;

        // Fetch the document
        try {
            document = Jsoup.connect(uri).get();
            document.select("th").remove();
        } catch (IOException e) {
            return stat;
        }

        // Select the table rows
        Elements table = document.select("tr");
        Stat contentStat;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        if(table.size()>0){
            ArrayList<Stat> contents = new ArrayList<>(table.size());
            for (Element row : table) {
                // Filter for removing queries, no links and links to parent directory
                // Remove Query Strings or no links
                if(row.toString().contains("href=?") || !row.toString().contains("href"))
                    continue;

                // Select the table data rows
                Elements rowContent = row.select("td");
                String fileName = rowContent.get(1).text();
                String dateString = rowContent.get(2).text();

                contentStat = new Stat();
                if (fileName.endsWith("/")) {
                    contentStat.name = fileName.substring(0, fileName.length() - 1);
                    contentStat.dir = true;
                    contentStat.file = false;
                } else {
                    contentStat.name = fileName;
                    contentStat.dir = false;
                    contentStat.file = true;
                    contentStat.size = SizeParser.getBytes(rowContent.get(3).text());
                }

                if (!dateString.equals("")) {
                    Date d = null;
                    try {
                        d = sdf.parse(dateString);
                        contentStat.time = d.getTime() / 1000L;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                contents.add(contentStat);
            }
            stat.setFiles(contents);
        }
        else {
            Elements links = document.select("a[href]");
            ArrayList<Stat> contents = new ArrayList<>(links.size());
            for (Element link : links) {
                //TODO: fix for parent directory (similar to above case
                contentStat = new Stat();
                String fileName = link.text();
                if(fileName.equals("Parent Directory"))
                    continue;
                if (fileName.endsWith("/")) {
                    contentStat.name = fileName.substring(0, fileName.length() - 1);
                    contentStat.dir = true;
                    contentStat.file = false;
                } else {
                    contentStat.name = fileName;
                    contentStat.dir = false;
                    contentStat.file = true;
                }
                contents.add(contentStat);
            }
            stat.setFiles(contents);
        }
        return stat;
    }

    public Stat exactStat() {
        Stat stat = new Stat();

        // Get the hostname from the uri
        stat.name = "Test.txt";
        stat.size = fetchFileSize(uri);
        stat.dir = false;
        stat.file = true;
        System.out.println(stat.size());
        return stat;
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return initialize()
                .map(HttpResource::exactStat);
    }

    private static int fetchFileSize(String urlString) {
        URL url;
        URLConnection conn = null;
        try {
            url = new URL(urlString);
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    private static class SizeParser {

        public static long getBytes(String sizeString) {
            int sizeStringLength = sizeString.length();
            char multiplier = sizeString.charAt(sizeStringLength - 1);
            long multiply = 1;
            switch (multiplier) {
                case 'T':
                    multiply <<= 40;
                case 'G':
                    multiply <<= 30;
                    break;
                case 'M':
                    multiply <<= 20;
                    break;
                case 'K':
                    multiply <<= 10;
            }

            if (multiplier != 1)
                sizeStringLength -= 1;

            String digits = sizeString.substring(0, sizeStringLength);
            // No size information available
            if (digits.equals(""))
                return 0;

            double size = Double.parseDouble(digits);

            size *= multiply;
            return Math.round(size);
        }
    }
}
