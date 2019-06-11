package org.onedatashare.server.module.http;

import org.apache.commons.vfs2.*;
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
        System.out.println("Error!!!!");
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
                //TODO: fix for parent directory (similar to above case
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

        // Fetch the document
        if (uri.endsWith("/"))
            try {
                document = Jsoup.connect(uri).get();
            } catch (IOException e) {
                System.out.println("In exact stat: is this a file???");
            }
        else
            try {
                fileObject = VFS.getManager().resolveFile(uri, new FileSystemOptions());
                // Get the hostname from the uri
                stat.name = getName(uri);
                stat.size = fileObject.getContent().getSize();
                stat.dir = false;
                stat.file = true;
                fileResource = true;
                totalSize += stat.size;
                fileList.add(stat);
            } catch (FileSystemException e) {
                System.out.println("In exact stat: is this a folder???");
                e.printStackTrace();
                return null;
            }
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

    public HttpTap tap() {
        return new HttpTap(uri);
    }

    class HttpTap implements Tap {

        FileContent fileContent;
        long size;

        public HttpTap(String uri) {
            FileSystemManager fileSystemManager;
            FileSystemOptions fileSystemOptions;
            try {
                fileSystemManager = VFS.getManager();
                fileSystemOptions = new FileSystemOptions();
                fileContent = fileSystemManager.resolveFile(uri, fileSystemOptions).getContent();

                byte[] b = new byte[(int)size];
                fileContent.getInputStream().read(b);
                System.out.println("WTG " + new String(b) + uri);
            } catch (FileSystemException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
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
                                finalInputStream.read(b, 0, sliceSizeInt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                        } else {
                            int remaining = sizeInt - state;
                            byte[] b = new byte[remaining];
                            try {
                                finalInputStream.read(b, 0, remaining);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                            sink.complete();
                        }
                        return state + sliceSizeInt;
                    });
        }
    }

}
