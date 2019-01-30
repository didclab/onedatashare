package org.onedatashare.server.controller;

import org.onedatashare.server.service.DbxOauthService;
import org.onedatashare.server.service.GoogleDriveOauthService;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.error.UnsupportedOperation;
import org.onedatashare.server.service.OauthService;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;

import java.util.Map;

@Controller
@RequestMapping("/api/stork/oauth")
public class OauthController {
  @Autowired
  private UserService userService;

  @Autowired
  private OauthService oauthService;

  @Autowired
  private GoogleDriveOauthService googleDriveOauthService;

  @Autowired
  private DbxOauthService dbxOauthService;

  String instance ="";
//  @GetMapping
//  public Mono<RedirectView> handle(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
//    if(queryParameters.containsKey("state")) {
//      return userService.saveCredential(headers.getFirst("cookie"), oauthService.finish(queryParameters.get("code")))
//        .map(uuid -> new RedirectView("/oauth/" + uuid));
//    }
//    else {
//      return userService.userLoggedIn(headers.getFirst("cookie")).map(oauthService::redirectToDropboxAuth);
//    }
//  }

  @GetMapping
  public Object handle(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
    String cookie = headers.getFirst("cookie");
      if(queryParameters.containsKey("state")) {
        if(instance.equals("googledrive")){
          instance = "";
          return userService.saveCredential(cookie, googleDriveOauthService.finish(queryParameters.get("code")))
                  .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
        }else if(instance.equals("dropbox")){
        instance = "";
        return userService.saveCredential(cookie, dbxOauthService.finish(queryParameters.get("code")))
                .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
        }else return null;
    }
    else {
      if(queryParameters.containsValue("googledrive")){
        instance = "googledrive";
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(googleDriveOauthService.start()).build());

      }else if(queryParameters.containsValue("dropbox")){
        instance = "dropbox";
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(dbxOauthService.start()).build());
      }else if(queryParameters.containsValue("dropbox")){
        instance = "gsiftp";
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(dbxOauthService.start()).build());
      }else return null;
    }
  }

  @ExceptionHandler(NotFound.class)
  public Object handle(NotFound notfound) {
    System.out.println(notfound.status);
    return Rendering.redirectTo("/404").build();
    //return new ResponseEntity<>(notfound, notfound.status);
  }

}


