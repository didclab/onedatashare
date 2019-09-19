package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.requestdata.TransferRequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stork/submit")
public class SubmitController {

  @Autowired
  private ResourceServiceImpl resourceService;

  @PostMapping
  public Object submit(@RequestHeader HttpHeaders headers, @RequestBody TransferRequestData transferRequestData) {
    String cookie = headers.getFirst(ODSConstants.COOKIE);
    UserAction userAction = UserAction.convertToUserAction(transferRequestData);
    return resourceService.submit(cookie, userAction);
  }
}
