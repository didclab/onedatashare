package org.onedatashare.server.controller;

import org.onedatashare.server.model.request.StopRequest;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.model.response.TransferJobSubmittedResponse;
import org.onedatashare.server.service.TransferJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
    public TransferJobController(TransferJobService transferJobService) {
        this.transferJobService = transferJobService;
    }


    Logger logger = LoggerFactory.getLogger(TransferJobController.class);

    @PostMapping
    public Mono<TransferJobSubmittedResponse> submit(@RequestBody TransferJobRequest request,
                                                     Mono<Principal> principalMono){
        logger.info("Recieved request: " + request.toString());
        return principalMono.flatMap(p -> transferJobService.submitTransferJobRequest(p.getName(), request));
    }
    @PostMapping
    public  Mono<Void> stopJob(@RequestBody StopRequest stopRequest){
        return transferJobService.stopTransferJob(stopRequest)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to stop job execution")));
    }
}
