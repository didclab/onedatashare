package org.onedatashare.server.controller;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.stream.IntStream;


@RestController
@RequestMapping("/api/stork/download")
public class DownloadController {

    @Autowired
    private DbxService dbxService;

    @Autowired
    private VfsService vfsService;

    @Autowired
    private ResourceServiceImpl resourceService;


    private static Mono<FileObject> sftpFileDownloadObj;

    @PostMapping
    public Object download(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
        String cookie = headers.getFirst("cookie");

        System.out.println(cookie);
        if (userAction.uri.startsWith("dropbox://")) {
            return dbxService.getDownloadURL(cookie, userAction);
        } else if ("googledrive:/".equals(userAction.type)) {
            if (userAction.credential == null) {
                return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            } else return resourceService.download(cookie, userAction);
        } else if (userAction.uri.startsWith("ftp://")) {

            return vfsService.getDownloadURL(cookie, userAction);
        } else if (userAction.uri.startsWith("sftp://")) {
            sftpFileDownloadObj = vfsService.getSftpDownload(cookie, userAction);
            return Mono.just("/api/stork/download/file");
        }
        return null;
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity> getAcquisition(@RequestHeader HttpHeaders clientHttpHeaders){//}, @RequestParam("data") String data) {
        String cookie = clientHttpHeaders.getFirst("cookie");

//        cookie

        if (sftpFileDownloadObj == null) {
            System.out.println("ERROR stream not set");
            return null;
        }
        return DownloadController.sftpFileDownloadObj.map(fileObject -> {

            InputStream inputStream = null;

            try {
                inputStream = fileObject.getContent().getInputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();
            }

//        System.out.println("Size of file is " + stream.length());
            String[] strings = fileObject.getName().toString().split("/");
            String filename = strings[strings.length - 1];
            System.out.println(filename);
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + filename);
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            ResponseEntity responseEntity = new ResponseEntity(inputStreamResource, httpHeaders, HttpStatus.OK);


            return responseEntity;

        });
    }
}
