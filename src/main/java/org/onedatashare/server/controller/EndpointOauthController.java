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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWebAuth;
import org.onedatashare.server.exceptionHandler.error.DuplicateCredentialException;
import org.onedatashare.server.exceptionHandler.error.NotFoundException;
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.service.CredentialService;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.oauth.BoxOauthService;
import org.onedatashare.server.service.oauth.DbxOauthService;
import org.onedatashare.server.service.oauth.GDriveOauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URI;
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
    private BoxOauthService boxOauthService;

    @Autowired
    private CredentialService credentialService;

    /**
     * Handler for google drive oauth requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/gdrive")
    public ModelAndView googleDriveOauthFinish(@RequestParam Map<String, String> queryParameters,
                                                         Principal principal) {
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
            return new ModelAndView("redirect:/transfer"+ errorStringBuilder);
        }
        OAuthEndpointCredential credential=gDriveOauthService.finish(queryParameters);
        credentialService.createCredential(credential, principal.getName(), EndpointType.gdrive);
        return new ModelAndView("redirect:/transfer?accountId=" + credential.getAccountId());
    }

    /**
     * Handler for google drive oauth requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/dropbox")
    public ModelAndView dropboxOauthFinish(@RequestParam Map<String, String> queryParameters,
                                                     Principal principal) throws DbxWebAuth.ProviderException, DbxWebAuth.NotApprovedException, DbxWebAuth.BadRequestException, DbxWebAuth.BadStateException, DbxException, DbxWebAuth.CsrfException {
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
            return new ModelAndView("redirect:/transfer"+ errorStringBuilder);
        }

        OAuthEndpointCredential credential=dbxOauthService.finish(queryParameters);
        credentialService.createCredential(credential, principal.getName(), EndpointType.dropbox);
        return new ModelAndView("redirect:/transfer?accountId=" + credential.getAccountId());
    }

    /**
     * Handler for Box requests
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping("/box")
    public ModelAndView boxOauthFinish(@RequestParam Map<String, String> queryParameters, Principal principal){
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
            return new ModelAndView("redirect:/transfer"+ errorStringBuilder);
        }

        OAuthEndpointCredential credential=boxOauthService.finish(queryParameters);
        credentialService.createCredential(credential, principal.getName(), EndpointType.box);

        return new ModelAndView("redirect:/transfer?accountId="+ credential.getAccountId());
    }

    @GetMapping
    public ModelAndView handle(@RequestParam EndpointType type) throws NotFoundException {
        return switch (type) {
            case box -> new ModelAndView(boxOauthService.start());
            case dropbox -> new ModelAndView(dbxOauthService.start());
            case gdrive -> new ModelAndView(gDriveOauthService.start());
            default -> throw new NotFoundException();
        };
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handle(NotFoundException notfound) {
        ODSLoggerService.logError(notfound.status.toString());
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(URI.create("/404"))
                .build();
    }

    @ExceptionHandler(DuplicateCredentialException.class)
    public ResponseEntity handle(DuplicateCredentialException dce) {
        ODSLoggerService.logError(dce.status.toString());
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(URI.create("/transfer"))
                .build();
    }
}