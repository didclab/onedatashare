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

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.error.DuplicateCredentialException;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.service.CredentialService;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.oauth.BoxOauthService;
import org.onedatashare.server.service.oauth.DbxOauthService;
import org.onedatashare.server.service.oauth.GDriveOauthService;
import org.onedatashare.server.service.oauth.GridFtpAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Map;

/**
 * Controller for handling OAuth control requests
 */
@Controller
@RequestMapping("/api/oauth")
public class EndpointOauthController {
    @Autowired
    private GDriveOauthService gDriveOauthService;

    @Autowired
    private DbxOauthService dbxOauthService;

    @Autowired
    private GridFtpAuthService gridFtpAuthService;

    @Autowired
    private BoxOauthService boxOauthService;

    @Autowired
    private CredentialService credentialService;

    /**
     * Handler for google drive oauth requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/gdrive")
    public Mono googleDriveOauthFinish(@RequestParam Map<String, String> queryParameters,
                                       Mono<Principal> principalMono) {
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

        return principalMono.map(Principal::getName)
                .flatMap(user -> gDriveOauthService.finish(queryParameters)
                        .flatMap(credential -> credentialService.createCredential(credential, user, EndpointType.gdrive)
                                .thenReturn(
                                        Rendering.redirectTo("/transfer?accountId=" + credential.getAccountId())
                                                .build()
                                )
                        )
                );
    }

    /**
     * Handler for google drive oauth requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/dropbox")
    public Mono dropboxOauthFinish(@RequestParam Map<String, String> queryParameters,
                                   Mono<Principal> principalMono) {
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

        return principalMono.map(Principal::getName)
                .flatMap(user -> dbxOauthService.finish(queryParameters)
                        .flatMap(credential -> credentialService.createCredential(credential, user, EndpointType.dropbox)
                                .thenReturn(
                                        Rendering.redirectTo("/transfer?accountId=" + credential.getAccountId())
                                                .build()
                                )
                        )
                );
    }

    /**
     * Handler for GridFTP requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/gridftp")
    public Mono gridftpOauthFinish(@RequestParam Map<String, String> queryParameters, Mono<Principal> principalMono) {
        if (!queryParameters.containsKey("code")) {
            return Mono.just(Rendering.redirectTo("/transfer").build());
        }

        return principalMono.map(Principal::getName)
                .flatMap(user -> gridFtpAuthService.finish(queryParameters)
                        .flatMap(credential -> credentialService.createCredential(credential, user, EndpointType.gridftp)
                                .thenReturn(
                                        Rendering.redirectTo("/transfer?accountId=" + credential.getAccountId())
                                                .build()
                                )
                        )
                );
    }

    /**
     * Handler for Box requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/box")
    public Mono boxOauthFinish(@RequestParam Map<String, String> queryParameters, Mono<Principal> principalMono){
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

        return principalMono.map(Principal::getName)
                .flatMap(user -> boxOauthService.finish(queryParameters)
                        .flatMap(credential -> credentialService.createCredential(credential, user, EndpointType.box)
                                .thenReturn(
                                        Rendering.redirectTo("/transfer?accountId=" + credential.getAccountId())
                                                .build()
                                )
                        )
                );
    }

    @GetMapping
    public Rendering handle(@RequestParam EndpointType type) throws NotFoundException {
        switch (type){
            case box:
                return Rendering.redirectTo(boxOauthService.start()).build();
            case dropbox:
                return Rendering.redirectTo(dbxOauthService.start()).build();
            case gdrive:
                return Rendering.redirectTo(gDriveOauthService.start()).build();
            case gridftp:
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