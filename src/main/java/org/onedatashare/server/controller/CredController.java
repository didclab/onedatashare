/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.controller;


import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller that returns the existing credentials (i.e. list of endpoints along with it's oauth tokens and other information)
 * linked to a user account.
 */
@RestController
@RequestMapping("/api/v1/stork/cred")
public class CredController {

  @Autowired
  private UserService userService;

  /**
   * Handler for the GET request for existing credentials linked to a user account.
   * User account is identified by the user email and password hash passed in the request header as a cookie
   *
   * @return a map containing all the endpoint credentials linked to the user account as a Mono
   */
  @GetMapping
  public Mono<Map<UUID, Credential>> listCredentials(@RequestHeader HttpHeaders headers) {
    return userService.getCredentials(null);
  }

  /**
   * Handler for the POST request for saving the OAuth Credentials once the user toggles it in account preferences
   * to save it.
   * @param headers - Request header
   * @param credentials - List of Credentials to save
   * @return
   */
  @PostMapping("/saveCredentials")
  public Mono<Void> saveCredentials(@RequestHeader HttpHeaders headers, @RequestBody List<OAuthCredential> credentials){
    String cookie = headers.getFirst(ODSConstants.COOKIE);
    return userService.saveUserCredentials(cookie,credentials);
  }

}
