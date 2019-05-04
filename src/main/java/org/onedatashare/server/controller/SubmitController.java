package org.onedatashare.server.controller;

import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stork/submit")
public class SubmitController {

  @Autowired
  private ResourceServiceImpl resourceService;

  @PostMapping
  public Object submit(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
    String cookie = headers.getFirst("cookie");
    return resourceService.submit(cookie, userAction);
  }
}
