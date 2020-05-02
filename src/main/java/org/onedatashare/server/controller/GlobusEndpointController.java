package org.onedatashare.server.controller;

import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.module.globusapi.EndPointList;
import org.onedatashare.module.globusapi.GlobusClient;
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

    @GetMapping("/endpoints/all")
    public Mono<EndPointList> getAllEndpoints(@RequestParam String filter){
        return userService.getGlobusClient(null)
                .flatMap(gc -> gc.getEndPointList(filter));
    }

    @GetMapping("/endpoint/{id}")
    public Mono<EndPoint> getEndpoint(@PathVariable String id){
        return userService.getGlobusClient(null)
                .flatMap(gc -> gc.getEndPoint(id));
    }

    @GetMapping("/endpoints")
    public Mono<Map<UUID, EndPoint>> getConnectedEndpoints() {
        return userService.getEndpointId(null);
    }

    @PostMapping("/endpoint")
    public Mono<Map<UUID, EndPoint>> addNewEndpoint(@RequestBody EndPoint endPoint){
        return userService.saveEndpointId(UUID.fromString(endPoint.getId()), endPoint, null);
    }

    @DeleteMapping("/endpoint/{id}")
    public Mono<Void> deleteConnectedEndpoint(@PathVariable String id){
        return userService.deleteEndpointId(null, UUID.fromString(id));
    }

    @PostMapping("/attempt-activation/{id}")
    public Mono<Void> attemptActivation(@PathVariable String id){
        return userService.getGlobusClient(null)
                .flatMap(globusClient -> globusClient.autoActivateEndPoint(id))
                .then();
    }

    @GetMapping("/endpoint-activate/{id}")
    public Mono<String> activateViaWebUri(@PathVariable String id){
        return Mono.just(GlobusClient.getGlobusEndpointActivationUri(id)) ;
    }
}