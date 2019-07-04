package org.onedatashare.server.controller;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.UserService;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


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
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        if (userAction.getUri().startsWith(ODSConstants.DROPBOX_URI_SCHEME)) {
            return dbxService.getDownloadURL(cookie, userAction);
        } else if (ODSConstants.DRIVE_URI_SCHEME.equals(userAction.getType())) {
            if (userAction.getCredential() == null) {
                return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            } else return resourceService.download(cookie, userAction);
        } else if (userAction.getUri().startsWith(ODSConstants.FTP_URI_SCHEME)) {
            return vfsService.getDownloadURL(cookie, userAction);
        }
        return null;
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    public Mono<ResponseEntity> getAcquisition(@RequestHeader HttpHeaders clientHttpHeaders) {
        String cookie = clientHttpHeaders.getFirst(ODSConstants.COOKIE);

        Map<String, String> map = new HashMap<String, String>();
        Set<Cookie> cookies = CookieDecoder.decode(cookie);
        for (Cookie c : cookies)
            map.put(c.getName(), c.getValue());
        ObjectMapper objectMapper = new ObjectMapper();
        UserActionResource userActionResource = null;
        try {
            final String credentials = URLDecoder.decode(map.get("SFTPAUTH"), "UTF-8");
            userActionResource = objectMapper.readValue(credentials, UserActionResource.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vfsService.getSftpDownloadStream(cookie, userActionResource);
    }
}
