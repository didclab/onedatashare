package org.onedatashare.server.controller;

import org.onedatashare.server.model.ScheduledTransferJobRequest;
import org.onedatashare.server.model.TransferJobRequestDTO;
import org.onedatashare.server.model.TransferParams;
import org.onedatashare.server.model.request.StopRequest;
import org.onedatashare.server.service.TransferSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.ws.rs.core.Response;
import java.security.Principal;
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
    public ResponseEntity<UUID> runJob(@RequestBody TransferJobRequestDTO request,
                                             Principal principal) {
        logger.debug("Recieved request: " + request.toString());
        request.setOwnerId(principal.getName());
        return ResponseEntity.ok(transferSchedulerService.scheduleJob(request));
    }

    @PostMapping("/stop")
    public ResponseEntity stopJob(@RequestBody StopRequest stopRequest) {
        try {
            return transferSchedulerService.stopTransferJob(stopRequest);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to stop job execution");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<ScheduledTransferJobRequest>> listScheduledJobs(@RequestParam String userEmail) {
        return ResponseEntity.ok(this.transferSchedulerService.listScheduledJobs(userEmail));
    }

    @GetMapping("/details")
    public ResponseEntity<TransferJobRequestDTO> getScheduledJob(@RequestParam UUID jobUuid) {
        return ResponseEntity.ok(this.transferSchedulerService.getJobDetails(jobUuid));
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteScheduledJob(@RequestParam UUID jobUuid) {
        this.transferSchedulerService.deleteScheduledJob(jobUuid);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/adjust")
    public ResponseEntity changeTransferParams(@RequestBody TransferParams transferParams) {
        return ResponseEntity.ok((this.transferSchedulerService.changeParams(transferParams)));
    }

}
