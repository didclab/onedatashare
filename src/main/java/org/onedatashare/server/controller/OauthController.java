package org.onedatashare.server.controller;

import org.onedatashare.server.model.error.DuplicateCredentialException;
import org.onedatashare.server.service.oauth.GoogleDriveOauthService;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.service.oauth.DbxOauthService;
import org.onedatashare.server.service.oauth.GridftpAuthService;
import org.onedatashare.server.service.oauth.OauthService;
import org.onedatashare.server.service.UserService;
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
  public UserService userService;

  @Autowired
  private OauthService oauthService;

  @Autowired
  private GoogleDriveOauthService googleDriveOauthService;

  @Autowired
  private DbxOauthService dbxOauthService;

  @Autowired
  private GridftpAuthService gridftpAuthService;

  static final String googledrive = "googledrive";
  static final String dropbox = "dropbox";
  static final String gridftp = "gridftp";

  @GetMapping(value = "/googledrive")
  public Object googledriveOauthFinish(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters){
    String cookie = headers.getFirst("cookie");
    return googleDriveOauthService.finish(queryParameters.get("code"), cookie)
            .flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
            .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build())
            .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredGoogleDrive" ).build()));
  }

  @GetMapping(value = "/dropbox")
  public Object dropboxOauthFinish(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters){
    String cookie = headers.getFirst("cookie");
    return dbxOauthService.finish(queryParameters.get("code"), cookie)
            .flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
            .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build())
            .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredDropbox" ).build()));
  }

  @GetMapping(value = "/gridftp")
  public Object gridftpOauthFinish(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters){
    String cookie = headers.getFirst("cookie");
    return gridftpAuthService.finish(queryParameters.get("code"))
            .flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
            .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
  }

  @GetMapping
  public Object handle(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
    String cookie = headers.getFirst("cookie");

      if(queryParameters.get("type").equals(googledrive) ){
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(googleDriveOauthService.start()).build());
      }else if(queryParameters.get("type").equals(dropbox) ){
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(dbxOauthService.start()).build());
      }else if(queryParameters.get("type").equals(gridftp) ){
        return userService.userLoggedIn(cookie)
                .map(bool -> Rendering.redirectTo(gridftpAuthService.start()).build());
      }else return Mono.error(new NotFound());

  }

  @ExceptionHandler(NotFound.class)
  public Object handle(NotFound notfound) {
    System.out.println(notfound.status);
    return Rendering.redirectTo("/404").build();

  }

  @ExceptionHandler(DuplicateCredentialException.class)
  public Object handleDup(DuplicateCredentialException dce) {
    System.out.println(dce.status);
    return Rendering.redirectTo("/transfer").build();
    //return new ResponseEntity<>(notfound, notfound.status);
  }
}


