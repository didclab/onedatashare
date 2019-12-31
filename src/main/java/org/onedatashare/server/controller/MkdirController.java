package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.requestdata.OperationRequestData;
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

/**
 * Controller for handling make directory requests on endpoints
 */
@RestController
@RequestMapping("/api/stork/mkdir")
public class MkdirController {

  @Autowired
  private DbxService dbxService;

  @Autowired
  private VfsService vfsService;

  @Autowired
  private ResourceServiceImpl resourceService;

  @Autowired
  private GridftpService gridService;

  /**
   * Handler that returns Mono of the stats(file information) in the given path of the endpoint
   * @param headers - Incoming request headers
   * @param operationRequestData - Request data needed to make a directory
   * @return Mono\<Stat\> containing the file/ folder information
   */
  @PostMapping
  public Object mkdir(@RequestHeader HttpHeaders headers, @RequestBody OperationRequestData operationRequestData) {

    UserAction userAction = UserAction.convertToUserAction(operationRequestData);
    String cookie = headers.getFirst(ODSConstants.COOKIE);
    if(userAction.getUri().contains(ODSConstants.DROPBOX_URI_SCHEME)) {
      if(userAction.getCredential() == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      else return dbxService.mkdir(cookie, userAction);
    }else if(ODSConstants.DRIVE_URI_SCHEME.equals(userAction.getType())) {
      if(userAction.getCredential() == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      else return resourceService.mkdir(cookie, userAction);
    }else if(ODSConstants.GRIDFTP_URI_SCHEME.equals(userAction.getType())) {
      if (userAction.getCredential() == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      } else return gridService.mkdir(cookie, userAction);
    }
    else return vfsService.mkdir(cookie, userAction);
  }

  @ExceptionHandler(AuthenticationRequired.class)
  public ResponseEntity<String> handle(AuthenticationRequired authenticationRequired) {
    return new ResponseEntity<>(authenticationRequired.toString(), authenticationRequired.status);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<String> handle(TokenExpiredException tokenExpiredException) {
    return new ResponseEntity<>(tokenExpiredException.toString(), tokenExpiredException.status);
  }


}
