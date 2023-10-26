package org.onedatashare.server.controller;

import org.onedatashare.server.model.ScheduledTransferJobRequest;
import org.onedatashare.server.model.TransferJobRequestDTO;
import org.onedatashare.server.model.request.StopRequest;
import org.onedatashare.server.service.TransferSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job")
public class TransferSchedulerController {

    private final TransferSchedulerService transferSchedulerService;

    public TransferSchedulerController(TransferSchedulerService transferSchedulerService) {
        this.transferSchedulerService = transferSchedulerService;
    }


    Logger logger = LoggerFactory.getLogger(TransferSchedulerController.class);

    @PostMapping("/schedule")
    public ResponseEntity<Mono<UUID>> runJob(@RequestBody TransferJobRequestDTO request,
                                             Principal principal) {
        logger.debug("Recieved request: " + request.toString());
        request.setOwnerId(principal.getName());
        return ResponseEntity.ok(transferSchedulerService.scheduleJob(request));
    }

    @PostMapping("/stop")
    public Mono<Void> stopJob(@RequestBody StopRequest stopRequest) {
        return transferSchedulerService.stopTransferJob(stopRequest)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to stop job execution")));
    }
    @GetMapping("/list")
    public ResponseEntity<Mono<List<ScheduledTransferJobRequest>>> listScheduledJobs(@RequestParam String userEmail) {
        return ResponseEntity.ok(this.transferSchedulerService.listScheduledJobs(userEmail));
    }

    @GetMapping("/details")
    public ResponseEntity<Mono<TransferJobRequestDTO>> getScheduledJob(@RequestParam UUID jobUuid) {
        return ResponseEntity.ok(this.transferSchedulerService.getJobDetails(jobUuid));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteScheduledJob(@RequestParam UUID jobUuid) {
        this.transferSchedulerService.deleteScheduledJob(jobUuid);
        return ResponseEntity.accepted().build();
    }

}
