package org.onedatashare.server.controller;


import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.GridftpService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.HttpFileService;

import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/stork/ls")
public class ListController {

    @Autowired
    private DbxService dbxService;

    @Autowired
    private VfsService vfsService;

    @Autowired
    private GridftpService gridService;

    @Autowired
    private ResourceServiceImpl resourceService;

    @Autowired
    private HttpFileService httpService;

    @PostMapping
    public Object list(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
        String cookie = headers.getFirst("cookie");

        if (userAction.credential == null) {
            switch (userAction.type) {
                case "dropbox:///":
                case "googledrive:/":
                case "gsiftp://":
                    return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        switch (userAction.type) {
            case "dropbox:///":
                return dbxService.list(cookie, userAction);
            case "googledrive:/":
                return resourceService.list(cookie, userAction);
            case "gsiftp://":
                return gridService.list(cookie, userAction);
            case "scp://":
            case "sftp://":
            case "ftp://":
                return vfsService.list(cookie, userAction);
            case "http://":
                return httpService.list(cookie, userAction);
            default:
                return null;
        }
    }

    @ExceptionHandler(AuthenticationRequired.class)
    public ResponseEntity<AuthenticationRequired> handle(AuthenticationRequired authenticationRequired) {
        return new ResponseEntity<>(authenticationRequired, authenticationRequired.status);
    }

//  public Mono<Stat> list(UserAction userAction){
//    Document document;
//    List<String> directoryContents = new ArrayList<>();
//    try {
//      document = Jsoup.connect(userAction.uri).get();
//      Elements links = document.select("a[href]");
//      for (Element link : links) {
//        directoryContents.add(link.text());
//        System.out.println(link.text());
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return Mono.just(new Stat());
//  }
}
