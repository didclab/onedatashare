package org.onedatashare.server.controller;

import com.onedatashare.commonutils.model.credential.AccountEndpointCredential;
import com.onedatashare.commonutils.model.credential.AccountCredentialType;
import com.onedatashare.commonutils.model.core.CredList;
import com.onedatashare.commonutils.model.credential.EndpointCredentialType;
import com.onedatashare.commonutils.service.auth.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cred/")
public class EndpointCredController {
    @Autowired
    private CredentialService credentialService;

    @PostMapping("{type}")
    public Object saveCredential(@RequestBody AccountEndpointCredential credential, @PathVariable AccountCredentialType type,
                                 Principal principal) {
        return credentialService.createCredential(credential, principal.getName(),
                        EndpointCredentialType.valueOf(type.toString()));
    }

    @GetMapping("{type}")
    public CredList getCredential(@PathVariable EndpointCredentialType type, Principal principal) {
        return credentialService.getStoredCredentialNames(principal.getName(), type);
    }

    @DeleteMapping("{type}/{credId}")
    public Object deleteCredential(@PathVariable String credId, @PathVariable EndpointCredentialType type,
                                           Principal principal) {
        return credentialService.deleteCredential(principal.getName(), type, credId);
    }
}
