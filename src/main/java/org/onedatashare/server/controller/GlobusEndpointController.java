package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.NotFoundException;
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
    @Autowired
    private UserService userService;
    @PostMapping
    public Object globusRequest(@RequestBody UserAction gea) {
        switch(gea.getAction()) {
            case "endpoint_list":
                return userService.getGlobusClient(null).flatMap(gc ->
                        gc.getEndPointList(gea.getFilter_fulltext()));
            case "endpoint":
                return userService.getGlobusClient(null)
                        .flatMap(gc -> gc.getEndPoint(gea.getGlobusEndpoint().getId()));
            case "endpointId":
                if(gea.getGlobusEndpoint().getId() == null){
                    return userService.getEndpointId(null);
                }
                return userService.saveEndpointId(UUID.fromString(gea.getGlobusEndpoint().getId()),
                                                  gea.getGlobusEndpoint(), null);
            case "deleteEndpointId":
                return userService.deleteEndpointId(null, UUID.fromString(gea.getGlobusEndpoint().getId()));
            case "endpointActivate":
                return userService.getGlobusClient(null).flatMap(gc ->
                    gc.activateEndPoint(gea.getGlobusEndpoint().getId(), gea.getGlobusEndpoint().getMyProxyServer(),
                                        gea.getGlobusEndpoint().getMyProxyDomainName(), gea.getUsername(), gea.getPassword()))
                    .switchIfEmpty(Mono.error(new Exception("Auth Failed")));
            default:
                return Mono.error(new NotFoundException());
        }
    }
}
