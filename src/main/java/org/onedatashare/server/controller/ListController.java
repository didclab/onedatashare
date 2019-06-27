package org.onedatashare.server.controller;

<<<<<<< HEAD

=======
import org.apache.http.protocol.HttpService;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.Stat;
>>>>>>> master
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

<<<<<<< HEAD
    @Autowired
    private HttpFileService httpService;


    @PostMapping
    public Object list(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
        String cookie = headers.getFirst("cookie");
        if (userAction.getCredential() == null) {
            switch (userAction.getType()) {
                case "dropbox:///":
                case "googledrive:/":
                case "gsiftp://":
                    return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        switch (userAction.getType()) {
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
=======
  @PostMapping
  public Object list(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
    String cookie = headers.getFirst(ODSConstants.COOKIE);

    if(userAction.getCredential() == null) {
      switch (userAction.getType()) {
        case ODSConstants.DROPBOX_URI_SCHEME:
        case ODSConstants.DRIVE_URI_SCHEME:
        case ODSConstants.GRIDFTP_URI_SCHEME:
          return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    switch (userAction.getType()){
      case ODSConstants.DROPBOX_URI_SCHEME:
        return dbxService.list(cookie, userAction);
      case ODSConstants.DRIVE_URI_SCHEME:
        return resourceService.list(cookie, userAction);
      case ODSConstants.GRIDFTP_URI_SCHEME:
        return gridService.list(cookie, userAction);
      case ODSConstants.SCP_URI_SCHEME:
      case ODSConstants.SFTP_URI_SCHEME:
      case ODSConstants.FTP_URI_SCHEME:
        return vfsService.list(cookie, userAction);
      case "http://":
      default:
        return null;
>>>>>>> master
    }

    @ExceptionHandler(AuthenticationRequired.class)
    public ResponseEntity<AuthenticationRequired> handle(AuthenticationRequired authenticationRequired) {
        return new ResponseEntity<>(authenticationRequired, authenticationRequired.status);
    }
}
