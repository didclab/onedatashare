package org.onedatashare.server.controller;

import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.model.response.TransferJobSubmittedResponse;
import org.onedatashare.server.service.TransferJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * This class is meant to take a valid TransferJobRequest and then forward that message to the transfer-scheduler which will aggregate credentials
 * for the request and expand the file system of all the user requested resources.
 *
 */
@RestController
@RequestMapping("/api/transferjob")
public class TransferJobController {
    @Autowired
    private TransferJobService transferJobService;

    @PostMapping
    public Mono<TransferJobSubmittedResponse> submit(@RequestBody TransferJobRequest request,
                                                     Mono<Principal> principalMono){
        return principalMono.flatMap(p -> transferJobService.submitTransferJobRequest(p.getName(), request));
    }
}
