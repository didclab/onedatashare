package org.onedatashare.server.controller;

import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stork/download")
public class DownloadController {

    @Autowired
    private DbxService dbxService;

    @Autowired
    private VfsService vfsService;

    @Autowired
    private ResourceServiceImpl resourceService;
    @PostMapping
    public Object download(@RequestHeader HttpHeaders headers,@RequestBody UserAction userAction){
        String cookie = headers.getFirst("cookie");
//        System.out.println(userAction);

        if(userAction.uri.startsWith("dropbox://")){
            return dbxService.getDownloadURL(cookie, userAction);
        }else if(userAction.uri.contains("googledrive:/")) {
            if(userAction.credential == null) {
                return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            else return resourceService.download(cookie, userAction);
        }//else return vfsService.download(cookie, userAction);

        return null;
    }
}
