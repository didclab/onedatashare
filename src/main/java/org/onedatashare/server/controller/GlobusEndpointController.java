package org.onedatashare.server.controller;

import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.module.globusapi.EndPointList;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stork/globus")
public class GlobusEndpointController {
    @Autowired
    private UserService userService;

    @GetMapping("/list-endpoints")
    public Mono<EndPointList> listEndpoints(@RequestParam String filter){
        return userService.getGlobusClient(null)
                .flatMap(gc -> gc.getEndPointList(filter));
    }

    @PostMapping("/attempt-activation/{id}")
    public Mono<Void> attemptActivation(@PathVariable String id){
        return userService.getGlobusClient(null)
                .flatMap(globusClient -> globusClient.autoActivateEndPoint(id))
                .then();
    }

    @GetMapping("/add-endpoint/")
    public Mono<Map<UUID, EndPoint>> addEndpoint(@PathVariable EndPoint endPoint){
        return userService.saveEndpointId(UUID.fromString(endPoint.getId()), endPoint, null);
    }

    @PostMapping
    public Object globusRequest(@RequestBody UserAction gea) {
        switch(gea.getAction()) {
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
