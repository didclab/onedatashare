package org.onedatashare.server.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.DuplicateCredentialException;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.oauth.GoogleDriveOauthService;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.service.oauth.DbxOauthService;
import org.onedatashare.server.service.oauth.GridftpAuthService;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/stork/oauth")
public class OauthController {
    @Autowired
    public UserService userService;

    @Autowired
    private GoogleDriveOauthService googleDriveOauthService;

    @Autowired
    private DbxOauthService dbxOauthService;

    @Autowired
    private GridftpAuthService gridftpAuthService;

    ObjectMapper objectMapper = new ObjectMapper();

    static final String googledrive = "googledrive";
    static final String dropbox = "dropbox";
    static final String gridftp = "gridftp";

    /**
     * Handler for google drive oauth requests
     * @param headers - Incoming request headers
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = "/googledrive")
    public Object googledriveOauthFinish(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);

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

        return userService.getLoggedInUser(cookie)
                .flatMap(user -> {
                    if (user.isSaveOAuthTokens()) {
                        return googleDriveOauthService.finish(queryParameters.get("code"), cookie)
                                .flatMap(oAuthCred -> userService.saveCredential(cookie, oAuthCred))
                                .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build())
                                .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredGoogleDrive").build()));
                    } else {
                        return googleDriveOauthService.finish(queryParameters.get("code"), cookie)
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
     * @param headers - Incoming request headers
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = "/dropbox")
    public Object dropboxOauthFinish(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);

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

        return userService.getLoggedInUser(cookie).flatMap(user -> {
            if (user.isSaveOAuthTokens())
                return dbxOauthService.finish(queryParameters.get("code"), cookie)
                        .flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
                        .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build())
                        .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredDropbox").build()));
            else
                return dbxOauthService.finish(queryParameters.get("code"), cookie)
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
     * @param headers - Incoming request headers
     * @param queryParameters - Query parameters
     * @return Mono\<String\>
     */
    @GetMapping(value = "/gridftp")
    public Object gridftpOauthFinish(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        return gridftpAuthService.finish(queryParameters.get("code"))
                .flatMap(oauthCred -> userService.saveCredential(cookie, oauthCred))
                .map(uuid -> Rendering.redirectTo("/oauth/" + uuid).build());
    }

    @GetMapping
    public Object handle(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> queryParameters) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);

        if (queryParameters.get("type").equals(googledrive)) {
            return userService.userLoggedIn(cookie)
                    .map(bool -> Rendering.redirectTo(googleDriveOauthService.start()).build());
        } else if (queryParameters.get("type").equals(dropbox)) {
            return userService.userLoggedIn(cookie)
                    .map(bool -> Rendering.redirectTo(dbxOauthService.start()).build());
        } else if (queryParameters.get("type").equals(gridftp)) {
            return userService.userLoggedIn(cookie)
                    .map(bool -> Rendering.redirectTo(gridftpAuthService.start()).build());
        } else return Mono.error(new NotFound());

    }

    @ExceptionHandler(NotFound.class)
    public Object handle(NotFound notfound) {
        ODSLoggerService.logError(notfound.status.toString());
        return Rendering.redirectTo("/404").build();

    }

    @ExceptionHandler(DuplicateCredentialException.class)
    public Object handleDup(DuplicateCredentialException dce) {
        ODSLoggerService.logError(dce.status.toString());
        return Rendering.redirectTo("/transfer").build();
    }
}


