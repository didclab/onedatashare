package org.onedatashare.server.controller;

import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.GridftpService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/delete")
public class DeleteController {
  @Autowired
  private DbxService dbxService;

  @Autowired
  private VfsService vfsService;

  @Autowired
  private ResourceServiceImpl resourceService;

  @Autowired
  private GridftpService gridService;

  private static final String EMAIL_PARAM = "email";
  private static final String HASH_PARAM = "hash";

  @PostMapping
  public Object delete(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {

    String temp = headers.getFirst("cookie");
    if(temp == null){
      //System.out.println("Email: "+userAction.getEmail()+" Hash: "+userAction.getPassword());
      if(userAction.getEmail()!=null && userAction.getPassword()!=null &&
              !userAction.getEmail().equalsIgnoreCase("") && !userAction.getPassword().equalsIgnoreCase("")){
        temp = EMAIL_PARAM + "=" + userAction.getEmail() + "; " +
                HASH_PARAM + "=" + userAction.getPassword();
      }
    }
    String cookie = temp;

    if(userAction.uri.contains("dropbox://")) {
      if(userAction.credential == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      else return dbxService.delete(cookie, userAction);
    }else if("googledrive:/".equals(userAction.type)) {
      if(userAction.credential == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      else return resourceService.delete(cookie, userAction);
    }else if("gsiftp://".equals(userAction.type)) {
      if (userAction.credential == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      } else return gridService.delete(cookie, userAction);
    }else return vfsService.delete(cookie, userAction);
  }

  @ExceptionHandler(AuthenticationRequired.class)
  public ResponseEntity<AuthenticationRequired> handle(AuthenticationRequired authenticationRequired) {
    return new ResponseEntity<>(authenticationRequired, authenticationRequired.status);
  }
}
