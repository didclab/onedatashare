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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/stork/globus")
public class GlobusEndpointController {
    Mono<GlobusClient> gc;
    @Autowired
    private UserService userService;
    @PostMapping
    public Object globusRequest(@RequestHeader HttpHeaders headers, @RequestBody UserAction gea) {
        switch(gea.action) {
            case "endpoint_list":
                return userService.getGlobusClient(headers.getFirst("cookie")).flatMap(gc ->
                        gc.getEndPointList("all", "0", "100", gea.filter_fulltext));
            case "endpoint":
                return userService.getGlobusClient(headers.getFirst("cookie")).flatMap(gc ->
                        gc.getEndPoint(gea.globusEndpoint.getId()));
            case "endpointId":
                if(gea.globusEndpoint.getId() == null){
                    return userService.getEndpointId(headers.getFirst("Cookie"));
                }
                return userService.saveEndpointId(
                        UUID.fromString(gea.globusEndpoint.getId()),
                        gea.globusEndpoint,
                        headers.getFirst("Cookie")
                );
            case "deleteEndpointId":
                return userService.deleteEndpointId(headers.getFirst("Cookie"), UUID.fromString(gea.getGlobusEndpoint().getId()));
            case "endpointActivate":
                return userService.getGlobusClient(headers.getFirst("cookie")).flatMap(gc ->
                    gc.activateEndPoint(gea.getGlobusEndpoint().getId(), gea.globusEndpoint.getMyProxyServer(), gea.globusEndpoint.getMyProxyDomainName(), gea.username, gea.password));
            default:
                return Mono.error(new NotFound());
        }
    }
}
