package org.onedatashare.server.controller;

import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stork/user")
public class UserController {
  @Autowired
  private UserService userService;

  @PostMapping
  public Object performAction(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
    if(userAction.action.equals("login")) {
      return userService.login(userAction.email, userAction.password);
    }
    else if(userAction.action.equals("register")) {
      return null;
    }
    else {
      return userService.saveHistory(userAction.uri, headers.getFirst("Cookie"));
    }
  }

  @GetMapping
  public Object getHistory(@RequestHeader HttpHeaders headers) {
    return userService.getHistory(headers.getFirst("Cookie"));
  }
}
