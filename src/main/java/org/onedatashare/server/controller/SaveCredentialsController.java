package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class SaveCredentialsController {
    @Autowired
    UserService userService;


//                            return googleDriveOauthService.finish(queryParameters.get("code"), cookie)
//            .flatMap(oAuthCred -> userService.saveCredential(cookie, oAuthCred))
//            .map(uuid -> Rendering.redirectTo("/oauth/uuid?identifier=" + uuid).build())
//            .switchIfEmpty(Mono.just(Rendering.redirectTo("/oauth/ExistingCredGoogleDrive").build()));

    @RequestMapping(name = "/api/stork/savecreds", method = RequestMethod.POST)
    public Mono<UUID> saveUserCredentials(@RequestHeader HttpHeaders httpHeaders, UserInfoCredential userActionCredential){
        String cookie = httpHeaders.getFirst(ODSConstants.COOKIE);
        return userService.saveCredential(cookie, userActionCredential);
    }
}
