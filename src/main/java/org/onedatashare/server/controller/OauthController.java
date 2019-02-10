package org.onedatashare.server.controller;

import org.onedatashare.server.model.error.DuplicateCredentialException;
import org.onedatashare.server.service.*;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.service.oauth.DbxOauthService;
import org.onedatashare.server.service.oauth.GoogleDriveOauthService;
import org.onedatashare.server.service.oauth.GridftpAuthService;
import org.onedatashare.server.service.oauth.OauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

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

  @Autowired
  private GridftpAuthService gridftpAuthService;

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
  static final String googledrive = "googledrive";
  static final String dropbox = "dropbox";
  static final String gridftp = "gridftp";
  @GetMapping
  public Object handle(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
    String cookie = headers.getFirst("cookie");
      if(queryParameters.containsKey("state")) {
        if(instance.equals(googledrive)){
          instance = "";
          return googleDriveOauthService.finish(queryParameters.get("code"), cookie).flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
                  .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
        }else if(instance.equals(dropbox)){
          instance = "";
        return dbxOauthService.finish(queryParameters.get("code"), cookie).flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
                .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
        }else if(instance.equals(gridftp)){
          instance = "";
          return gridftpAuthService.finish(queryParameters.get("code")).flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
                  .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
        }else return Mono.error(new NotFound());
    }
    else {
      if(queryParameters.get("type").equals(googledrive) ){
        instance = googledrive;
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(googleDriveOauthService.start()).build());

      }else if(queryParameters.get("type").equals(dropbox) ){
        instance = dropbox;
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(dbxOauthService.start()).build());
      }else if(queryParameters.get("type").equals(gridftp) ){
        instance = gridftp;
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(gridftpAuthService.start()).build());
      }else return Mono.error(new NotFound());
    }
  }

  @ExceptionHandler(NotFound.class)
  public Object handle(NotFound notfound) {
    System.out.println(notfound.status);
    return Rendering.redirectTo("/404").build();
    //return new ResponseEntity<>(notfound, notfound.status);
  }

  @ExceptionHandler(DuplicateCredentialException.class)
  public Object handleDup(DuplicateCredentialException dce) {
    System.out.println(dce.status);
    return Rendering.redirectTo("/transfer").build();
    //return new ResponseEntity<>(notfound, notfound.status);
  }
}


