package org.onedatashare.server.controller;

import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/submit")
public class SubmitController {

  @Autowired
  private DbxService dbxService;

  @Autowired
  private VfsService vfsService;

  @Autowired
  private ResourceServiceImpl resourceService;

  private static final String EMAIL_PARAM = "email";
  private static final String HASH_PARAM = "hash";

  @PostMapping
  public Object submit(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
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

    return resourceService.submit(cookie, userAction);
  }
}
