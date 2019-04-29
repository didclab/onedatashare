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
@RequestMapping("/api/stork/cred")
public class CredController {

  @Autowired
  private UserService userService;

  @GetMapping
  public Mono<Map<String, Credential>> listCredentials(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
    return userService.getCredentials(headers.getFirst("cookie"));
  }

}
