package org.onedatashare.server.controller;


import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/cred")
public class CredController {

  @Autowired
  private UserService userService;

  private static final String EMAIL_PARAM = "email";
  private static final String HASH_PARAM = "hash";

  @GetMapping
  public Mono<Map<UUID, Credential>> listCredentials(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
    String temp = headers.getFirst("cookie");

    if(temp == null){
      if(queryParameters.containsKey(EMAIL_PARAM) && queryParameters.containsKey(HASH_PARAM)){
        temp = EMAIL_PARAM + "=" + queryParameters.get(EMAIL_PARAM) + "; " +
                HASH_PARAM + "=" + queryParameters.get(HASH_PARAM);
      }
    }

    String cookie = temp;

    return userService.getCredentials(cookie);
  }

}
