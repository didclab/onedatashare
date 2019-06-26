package org.onedatashare.server.module.http;

import org.apache.commons.vfs2.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.core.Slice;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.core.Tap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

        if (table.size() > 0) {
            ArrayList<Stat> contents = new ArrayList<>(table.size());
            for (Element row : table) {
                // Filter for removing queries, no links and links to parent directory
                // Remove Query Strings or no links
                if (row.toString().contains("href=?") || !row.toString().contains("href"))
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
        } else {
            Elements links = document.select("a[href]");
            ArrayList<Stat> contents = new ArrayList<>(links.size());
            for (Element link : links) {
                contentStat = new Stat();
                String fileName = link.text();
                if (fileName.equals("Parent Directory"))
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

        FileObject fileObject = null;

        Document document = null;
        List<Stat> fileList = new LinkedList<>();
        long totalSize = 0;

        boolean exception = false;

        // Fetch the document
        if (!uri.endsWith("/"))
            try {
                Connection.Response resp = Jsoup.connect(uri).execute();

                System.out.println("Type " + resp.contentType());

                if (!resp.contentType().contains("html"))
                    throw new Exception("Not a html page");

                document = Jsoup.connect(uri).get();
                Elements elements = document.select("a[href]");

                stat.name = uri.substring(uri.lastIndexOf('/') + 1);
                System.out.println("Name is  " + stat.name);
                stat.dir = true;
                stat.file = false;

                for (Element e : elements) {
                    // Ignore Folders
                    if (e.text().endsWith("/"))
                        continue;
                    Stat tempStat = new Stat();
                    tempStat.dir = false;
                    tempStat.file = true;
                    tempStat.name = "/" + e.text();
                    try {
                        tempStat.size = VFS.getManager().resolveFile(uri + "/" + tempStat.name).getContent().getSize();
                        tempStat.id = uri + "/" + tempStat.name;
                        System.out.println("ID "  + tempStat.id);
                        totalSize += tempStat.size;
                        fileList.add(tempStat);
                    } catch (Exception e1) {
                        System.out.println("Skipped " + tempStat.name);
                    }
                }
            } catch (Exception e) {
                exception = true;
            }

        if (exception)
            try {
                fileObject = VFS.getManager().resolveFile(uri, new FileSystemOptions());
                // Get the hostname from the uri
                stat.name = getName(uri);
                stat.size = fileObject.getContent().getSize();
                stat.id = uri;
                stat.dir = false;
                stat.file = true;
                totalSize += stat.size;
                fileList.add(stat);
            } catch (FileSystemException e) {
                System.out.println("In exact stat: is this a folder???");
                e.printStackTrace();
                return null;
            }
        stat.setFiles(fileList);
        stat.setFilesList(fileList);
        stat.setSize(totalSize);

        return stat;
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return initialize()
                .map(HttpResource::exactStat);
    }

    private static String getName(String uri) {
        String name = null;
        String[] splitURI = uri.split("/");
        if (splitURI.length > 0)
            name = splitURI[splitURI.length - 1];
        return name;
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

    public HttpTap tap() {
        return new HttpTap();
    }

    class HttpTap implements Tap {

        FileContent fileContent;
        long size;

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {

            try {
                FileObject fileObject = VFS.getManager().resolveFile(stat.id);
                fileContent = fileObject.getContent();
            } catch (FileSystemException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            size = stat.getSize();
            return tap(sliceSize);
        }


        public Flux<Slice> tap(long sliceSize) {
            int sliceSizeInt = Math.toIntExact(sliceSize);
            int sizeInt = Math.toIntExact(size);
            InputStream inputStream = null;
            try {
                inputStream = fileContent.getInputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            InputStream finalInputStream = inputStream;
            return Flux.generate(
                    () -> 0,
                    (state, sink) -> {
                        if (state + sliceSizeInt < sizeInt) {
                            byte[] b = new byte[sliceSizeInt];
                            try {
                                finalInputStream.read(b);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                        } else {
                            int remaining = sizeInt - state;
                            byte[] b = new byte[remaining];

                            // Fix for corrupted PDF Files - Added by Yifu
                            try {
                                int offset = 0;
                                for(; offset < remaining-1; offset+=1){
                                    finalInputStream.read(b, offset, 1);
                                }
                                finalInputStream.read(b, offset, remaining-offset);

                                sink.next(new Slice(b));
                                finalInputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            sink.complete();
                            return state + remaining;
                        }
                        return state + sliceSizeInt;
                    });
        }
    }

}