package org.onedatashare.server.controller;

import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.service.TransferJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transfer-job")
public class TransferJobController {
    @Autowired
    private TransferJobService transferJobService;

    @PostMapping
    public Mono<Void> submit(@RequestBody TransferJobRequest request){
        return transferJobService.submitRequest(request);
    }
}
