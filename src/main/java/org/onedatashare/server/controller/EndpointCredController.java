package org.onedatashare.server.controller;

import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.node.TextNode;
import org.onedatashare.server.model.core.AuthType;
import org.onedatashare.server.model.core.CredList;
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.service.CredentialService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.HashMap;
//import java.util.logging.Logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/cred/")
public class EndpointCredController {
    @Autowired
    private CredentialService credentialService;
    Logger logger = LoggerFactory.getLogger(EndpointCredController.class);

    @PostMapping("{type}")
    public Mono<Object> saveCredential(@RequestBody AccountEndpointCredential credential, @PathVariable AuthType type,
                                           Mono<Principal> principalMono){
        return principalMono.map(Principal::getName)
                .flatMap(user -> credentialService.createCredential(credential, user,
                        EndpointType.valueOf(type.toString())));
    }

    @GetMapping("{type}")
    public Mono<CredList> getCredential(@PathVariable EndpointType type, Mono<Principal> principalMono){
//        return principalMono.map(Principal::getName)
//                .flatMap(user -> credentialService.getStoredCredentialNames(user, type));
        return principalMono.map(Principal::getName)
                .flatMap(user -> {
                    Mono<CredList> monoList = credentialService.getStoredCredentialNames(user, type);
                    monoList.map(credList -> {
                        for(String cred : credList.getList()){
                            logger.info(cred);
                        }
                        return credList;
                    });
                    return monoList;
                });
    }

    @DeleteMapping("{type}/{credId}")
    public Mono<Void> deleteCredential(@PathVariable String credId, @PathVariable EndpointType type,
                                       Mono<Principal> principalMono) {
        logger.info("Hit the delete controller with {} type is {}", credId, type);
        return principalMono.map(Principal::getName)
                .flatMap(user->credentialService.deleteCredential(user, type, credId));
    }

//    @PatchMapping("{type}")
//    public Mono<Void> deleteCredential(@RequestBody HashMap credential, @PathVariable EndpointType type,
//                                       Mono<Principal> principalMono) {
//        System.out.println("deleting credential {type}");
//        return principalMono.map(Principal::getName)
//                .flatMap(user->credentialService.deleteCredential(user, type, credential.get("credential").toString()));
//    }
}
