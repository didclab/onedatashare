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
import org.onedatashare.server.service.ODSLoggerService;
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

    private static final char TERA ='T', GIGA = 'G', MEGA = 'M', KILO = 'K';

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


    /**
     * This method returns the information parsed from the given http file server web page
     * @return
     * Returns the stat object
     */
    public Stat onStat() {
        Stat stat = new Stat();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Get the hostname from the uri
        stat.setName(URI.create(uri).toString());

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

        // If the web page uses a table
        if (table.size() > 0) {
            ArrayList<Stat> contents = new ArrayList<>(table.size());
            for (Element row : table) {
                // Filter for removing queries, no links and links to parent directory
                if (row.toString().contains("href=?") || !row.toString().contains("href"))
                    continue;

                // Select the table rows
                Elements rowContent = row.select("td");
                String fileName = rowContent.get(1).text();
                String dateString = rowContent.get(2).text();

                contentStat = new Stat();
                /* Name ending with / is a folder*/
                if (fileName.endsWith("/")) {
                    contentStat.setName(fileName.substring(0, fileName.length() - 1));
                    contentStat.setDir(true);
                    contentStat.setFile(false);
                }
                /* Else a file */
                else {
                    contentStat.setName(fileName);
                    contentStat.setDir(false);
                    contentStat.setFile(true);
                    contentStat.setSize(getBytes(rowContent.get(3).text()));
                }

                if (!dateString.equals("")) {
                    Date d = null;
                    try {
                        d = sdf.parse(dateString);
                        contentStat.setTime(d.getTime() / 1000L);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                contents.add(contentStat);
            }
            stat.setFiles(contents);
        }
        // If the web page format isn't known
        else {
            Elements links = document.select("a[href]");
            ArrayList<Stat> contents = new ArrayList<>(links.size());
            for (Element link : links) {
                contentStat = new Stat();
                String fileName = link.text();
                // Skip adding links to the parent directory
                if (fileName.equals("Parent Directory"))
                    continue;
                // If the file is a directory*/
                if (fileName.endsWith("/")) {
                    contentStat.setName(fileName.substring(0, fileName.length() - 1));
                    contentStat.setDir(true);
                    contentStat.setFile(false);
                }
                // Else a folder
                else {
                    contentStat.setName(fileName);
                    contentStat.setDir(false);
                    contentStat.setFile(true);
                }
                contents.add(contentStat);
            }
            stat.setFiles(contents);
        }
        return stat;
    }

    /**
     * This method returns the correct size of the file / directory with upto 1 level depth
     * @return Returns a Stat object
     */
    public Stat exactStat() {
        Stat stat = new Stat();

        FileObject fileObject = null;

        Document document = null;
        List<Stat> fileList = new LinkedList<>();
        long totalSize = 0;

        boolean exception = false;

        // If a link i.e. Directory
        if (!uri.endsWith("/"))
            try {
                Connection.Response resp = Jsoup.connect(uri).execute();

                if (!resp.contentType().contains("html"))
                    throw new Exception("Not a html page");

                document = Jsoup.connect(uri).get();
                Elements elements = document.select("a[href]");

                stat.setName(uri.substring(uri.lastIndexOf('/') + 1));
                stat.setDir(true);
                stat.setFile(false);

                for (Element e : elements) {
                    // Ignore Folders
                    if (e.text().endsWith("/"))
                        continue;
                    Stat tempStat = new Stat();
                    tempStat.setDir(false);
                    tempStat.setFile(true);
                    tempStat.setName("/" + e.text());
                    try {
                        tempStat.setSize(VFS.getManager().resolveFile(uri + "/" + tempStat.getName()).getContent().getSize());
                        tempStat.setId(uri + "/" + tempStat.getName());
                        totalSize += tempStat.getSize();
                        fileList.add(tempStat);
                    } catch (Exception e1) {
                        ODSLoggerService.logError("Skipped " + tempStat.getName());
                    }
                }
            } catch (Exception e) {
                exception = true;
            }

        /* If a file*/
        if (exception)
            try {
                fileObject = VFS.getManager().resolveFile(uri, new FileSystemOptions());
                // Get the hostname from the uri
                stat.setName(getName(uri));
                stat.setSize(fileObject.getContent().getSize());
                stat.setId(uri);
                stat.setDir(false);
                stat.setFile(false);
                totalSize += stat.getSize();
                fileList.add(stat);
            } catch (FileSystemException e) {
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

    /**
     * parses the Size String from the webpage
     */
    public static long getBytes(String sizeString) {
        int sizeStringLength = sizeString.length();
        char multiplier = sizeString.charAt(sizeStringLength - 1);
        long multiply = 1;
        switch (Character.toUpperCase(multiplier)) {
            case TERA:
                multiply <<= 10;
            case GIGA:
                multiply <<= 10;
            case MEGA:
                multiply <<= 10;
            case KILO:
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

    public HttpTap tap() {
        return new HttpTap();
    }

    class HttpTap implements Tap {

        FileContent fileContent;
        long size;


        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {

            try {
                FileObject fileObject = VFS.getManager().resolveFile(stat.getId());
                fileContent = fileObject.getContent();
            } catch (FileSystemException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            size = stat.getSize();
            return tap(sliceSize);
        }

        /**
         * This is the tap method that is used to fetch information from the given HTTP file server
         * @param sliceSize : Size of the chunk to be fetched
         * @return Returns a Flux of Slice
         */
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
                                // Fix for buggy PDF files - Else the PDF files are corrupted
                                for(int offset = 0; offset < sliceSizeInt; offset+=1)
                                    finalInputStream.read(b, offset, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sink.next(new Slice(b));
                        } else {
                            int remaining = sizeInt - state;
                            byte[] b = new byte[remaining];
                            try {
                                // Fix for buggy PDF files - Else the PDF files are corrupted
                                for(int offset = 0; offset < remaining; offset+=1)
                                    finalInputStream.read(b, offset, 1);
                                sink.next(new Slice(b));
                                finalInputStream.close();
                                sink.complete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return state + remaining;
                        }
                        return state + sliceSizeInt;
                    });
        }
    }

}
