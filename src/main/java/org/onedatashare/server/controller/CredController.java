package org.onedatashare.server.controller;


import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Controller that returns the existing credentials (i.e. list of endpoints along with it's oauth tokens and other information)
 * linked to a user account.
 */
@RestController
@RequestMapping("/api/stork/cred")
public class CredController {

  @Autowired
  private UserService userService;

  /**
   * Handler for the GET request for existing credentials linked to a user account.
   * User account is identified by the user email and password hash passed in the request header as a cookie
   *
   * @param headers - Request header
   * @param queryParameters - Request query parameters
   * @return a map containing all the endpoint credentials linked to the user account as a Mono
   */
  @GetMapping
  public Mono<Map<UUID, Credential>> listCredentials(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
    return userService.getCredentials(headers.getFirst(ODSConstants.COOKIE));
  }

}
