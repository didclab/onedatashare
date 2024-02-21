package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.AuthType;
import org.onedatashare.server.model.core.CredList;
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/cred/")
public class EndpointCredController {
    @Autowired
    private CredentialService credentialService;

    @PostMapping("{type}")
    public Object saveCredential(@RequestBody AccountEndpointCredential credential, @PathVariable AuthType type,
                                         Principal principal) {
        return credentialService.createCredential(credential, principal.getName(),
                        EndpointType.valueOf(type.toString()));
    }

    @GetMapping("{type}")
    public CredList getCredential(@PathVariable EndpointType type, Principal principal) {
        return credentialService.getStoredCredentialNames(principal.getName(), type);
    }

    @DeleteMapping("{type}/{credId}")
    public Object deleteCredential(@PathVariable String credId, @PathVariable EndpointType type,
                                           Principal principal) {
        return credentialService.deleteCredential(principal.getName(), type, credId);
    }
}
