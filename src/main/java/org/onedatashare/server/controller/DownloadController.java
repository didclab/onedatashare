package org.onedatashare.server.controller;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.UserService;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.VfsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    @Autowired
    private UserService userService;


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
        }
        return null;
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    public Mono<ResponseEntity> getAcquisition(@RequestHeader HttpHeaders clientHttpHeaders) {
        String cookie = clientHttpHeaders.getFirst("cookie");
        final String authorization = clientHttpHeaders.getFirst("Authorization");
        String credentials = null;
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            credentials = authorization.substring("Basic".length()).trim();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        UserActionResource userActionResource = null;
        try {
            userActionResource = objectMapper.readValue(credentials, UserActionResource.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vfsService.getSftpDownloadStream(cookie, userActionResource);
    }
}
