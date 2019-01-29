package org.onedatashare.server.controller;

import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.DbxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stork/download")
public class DownloadController {

    @Autowired
    private DbxService dbxService;

    @PostMapping
    public Object download(@RequestHeader HttpHeaders headers,@RequestBody UserAction userAction){
        String cookie = headers.getFirst("cookie");
//        System.out.println(userAction);

        if(userAction.uri.startsWith("dropbox://")){
            return dbxService.getDownloadURL(cookie, userAction);
        }

        return null;
    }
}
