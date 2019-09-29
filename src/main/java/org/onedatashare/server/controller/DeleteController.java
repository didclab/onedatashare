package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
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
 * Controller that deletes a given file/ folder on multiple endpoints
 */
@RestController
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

  /**
   *  Handler for the delete request of a file or folder made on an endpoint.
   * @param headers - Request Header
   * @param operationRequestData - Required Data to perform the delete operation on the endpoint
   * @return Mono\<Resource\>
   */
  @PostMapping
  public Object delete(@RequestHeader HttpHeaders headers, @RequestBody OperationRequestData operationRequestData) {
    UserAction userAction = UserAction.convertToUserAction(operationRequestData);
    String cookie = headers.getFirst(ODSConstants.COOKIE);
    if(userAction.getUri().contains(ODSConstants.DROPBOX_URI_SCHEME)) {
      if(userAction.getCredential() == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      else return dbxService.delete(cookie, userAction);
    }else if(ODSConstants.DRIVE_URI_SCHEME.equals(userAction.getType())) {
      if(userAction.getCredential() == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      else return resourceService.delete(cookie, userAction);
    }else if(ODSConstants.GRIDFTP_URI_SCHEME.equals(userAction.getType())) {
      if (userAction.getCredential() == null) {
        return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
      } else return gridService.delete(cookie, userAction);
    }else{
      return vfsService.delete(cookie, userAction);
    }
  }

  @ExceptionHandler(AuthenticationRequired.class)
  public ResponseEntity<AuthenticationRequired> handle(AuthenticationRequired authenticationRequired) {
    return new ResponseEntity<>(authenticationRequired, authenticationRequired.status);
  }
}
