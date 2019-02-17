package org.onedatashare.server.controller;

import org.onedatashare.module.globusapi.EndPointList;
import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.useraction.GlobusEndpointAction;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stork/globus")
public class GlobusEndpointController {
    Mono<GlobusClient> gc;
    @Autowired
    private UserService userService;
    @PostMapping
    public Mono<EndPointList> globusRequest(@RequestHeader HttpHeaders headers, @RequestBody GlobusEndpointAction gea) {
        if(gea.action.equals("endpoint_list")) {
            String cookie = headers.getFirst("cookie");
            gc = getGlobusClient(cookie);
            return gc.flatMap(gc ->
                gc.getEndPointList("all", "0", "100", gea.filter_fulltext));
        }
        return Mono.error(new NotFound());
    }

    public Mono<GlobusClient> getGlobusClient(String cookie){
        return userService.getLoggedInUser(cookie)
            .map(user -> {
                for (Credential credential : user.getCredentials().values()) {
                    if (credential.type == Credential.CredentialType.OAUTH) {
                        OAuthCredential oaucr = (OAuthCredential) credential;
                        if (oaucr.name.contains("GridFTP")) {
                            return new GlobusClient(oaucr.token);
                        }
                    }
                }
                return new GlobusClient();
            });
    }
}
