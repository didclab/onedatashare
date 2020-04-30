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

import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.error.DuplicateCredentialException;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.oauth.*;

import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Controller for handling OAuth control requests
 */
@Controller
@RequestMapping("/api/oauth")
public class OauthController {
    @Autowired
    public UserService userService;

    @Autowired
    private GoogleDriveOauthService googleDriveOauthService;

    @Autowired
    private DbxOauthService dbxOauthService;

    @Autowired
    private GridFtpAuthService gridFtpAuthService;

    @Autowired
    private BoxOauthService boxOauthService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String gDrive = "gdrive";
    private static final String dropbox = "dropbox";
    private static final String gridFtp = "gftp";
    private static final String box = "box";

    /**
     * Handler for google drive oauth requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = gDrive)
    public Object googledriveOauthFinish(@RequestParam Map<String, String> queryParameters) {

        if (!queryParameters.containsKey("code")) {
            StringBuilder errorStringBuilder = new StringBuilder();
            if (queryParameters.containsKey("error")) {
                try {
                    errorStringBuilder.append(URLEncoder.encode(queryParameters.get("error"), "UTF-8"));
                    errorStringBuilder.insert(0, "?error=");
                } catch (UnsupportedEncodingException e) {
                    ODSLoggerService.logError("Invalid error message received from Google Drive " +
                            "oauth after cancellation" + queryParameters.get("error_description"));
                }
            }
            return Mono.just(Rendering.redirectTo("/transfer" + errorStringBuilder.toString()).build());
        }

        return userService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.isSaveOAuthTokens()) {
                        return googleDriveOauthService.finish(queryParameters)
                                .flatMap(oAuthCred -> userService.saveCredential(null, oAuthCred))
                                .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build())
                                .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredGoogleDrive").build()));
                    } else {
                        return googleDriveOauthService.finish(queryParameters)
                                .map(oAuthCred -> {
                                    try {
                                        return "/oauth/googledrive?creds=" +
                                                URLEncoder.encode(objectMapper.writeValueAsString(oAuthCred), StandardCharsets.UTF_8.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                })
                                .map(oAuthCredLink -> Rendering.redirectTo(oAuthCredLink).build())
                                .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredGoogleDrive").build()));
                    }
                });
    }

    /**
     * Handler for google drive oauth requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = dropbox)
    public Object dropboxOauthFinish(@RequestParam Map<String, String> queryParameters) {

        if (!queryParameters.containsKey("code")) {
            StringBuilder errorStringBuilder = new StringBuilder();
            if (queryParameters.containsKey("error_description")) {
                try {
                    errorStringBuilder.append(URLEncoder.encode(queryParameters.get("error_description"), "UTF-8"));
                    errorStringBuilder.insert(0, "?error=");
                } catch (UnsupportedEncodingException e) {
                    ODSLoggerService.logError("Invalid error message received from DropBox " +
                            "oauth after cancellation" + queryParameters.get("error_description"));
                }
            }
            return Mono.just(Rendering.redirectTo("/transfer" + errorStringBuilder.toString()).build());
        }

        return userService.getLoggedInUser().flatMap(user -> {
            if (user.isSaveOAuthTokens())
                return dbxOauthService.finish(queryParameters)
                        .flatMap(oauthCred -> userService.saveCredential(null, oauthCred))
                        .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build())
                        .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredDropbox").build()));
            else
                return dbxOauthService.finish(queryParameters)
                        .map(oAuthCredential -> {
                            try {
                                return "/oauth/dropbox?creds=" + URLEncoder.encode(objectMapper.writeValueAsString(oAuthCredential), StandardCharsets.UTF_8.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .map(oauthCred -> Rendering.redirectTo(oauthCred).build())
                        .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredDropbox").build()));
        });
    }

    /**
     * Handler for GridFTP requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = gridFtp)
    public Object gridftpOauthFinish(@RequestParam Map<String, String> queryParameters) {
        return gridFtpAuthService.finish(queryParameters)
                .flatMap(oauthCred -> userService.saveCredential(null, oauthCred))
                .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build());
    }
    /**
     * Handler for Box requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = box)
    public Object boxOauthFinish(@RequestParam Map<String, String> queryParameters){
        if (!queryParameters.containsKey("code")) {
            StringBuilder errorStringBuilder = new StringBuilder();
            if (queryParameters.containsKey("error")) {
                try {
                    errorStringBuilder.append(URLEncoder.encode(queryParameters.get("error"), "UTF-8"));
                    errorStringBuilder.insert(0, "?error=");
                } catch (UnsupportedEncodingException e) {
                    ODSLoggerService.logError("Invalid error message received from Google Drive " +
                            "oauth after cancellation" + queryParameters.get("error_description"));
                }
            }
            return Mono.just(Rendering.redirectTo("/transfer" + errorStringBuilder.toString()).build());
        }

        return userService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.isSaveOAuthTokens()) {
                        return boxOauthService.finish(queryParameters)
                                .flatMap(oAuthCred -> userService.saveCredential(null, oAuthCred))
                                .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build())
                                .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredBox").build()));
                    } else {
                        return boxOauthService.finish(queryParameters)
                                .map(oAuthCred -> {
                                    try {
                                        return "/oauth/box?creds=" +
                                                URLEncoder.encode(objectMapper.writeValueAsString(oAuthCred), StandardCharsets.UTF_8.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                })
                                .map(oAuthCredLink -> Rendering.redirectTo(oAuthCredLink).build())
                                .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredBox").build()));
                    }
                });
    }

    @GetMapping
    public Rendering handle(@RequestParam String type) throws NotFoundException {
        switch (type){
            case box:
                return Rendering.redirectTo(boxOauthService.start()).build();
            case dropbox:
                return Rendering.redirectTo(dbxOauthService.start()).build();
            case gDrive:
                return Rendering.redirectTo(googleDriveOauthService.start()).build();
            case gridFtp:
                return Rendering.redirectTo(gridFtpAuthService.start()).build();
            default:
                throw new NotFoundException();
        }
    }

    @ExceptionHandler(NotFoundException.class)
    public Rendering handle(NotFoundException notfound) {
        ODSLoggerService.logError(notfound.status.toString());
        return Rendering.redirectTo("/404").build();
    }

    @ExceptionHandler(DuplicateCredentialException.class)
    public Rendering handle(DuplicateCredentialException dce) {
        ODSLoggerService.logError(dce.status.toString());
        return Rendering.redirectTo("/transfer").build();
    }
}


