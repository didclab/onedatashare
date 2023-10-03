package org.onedatashare.server.controller;

import org.onedatashare.server.model.TransferJobRequestDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@RestController("/job")
public class TransferSchedulerController {

    @PostMapping("/schedule")
    public ResponseEntity<UUID> scheduleJob(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobStartTime, @RequestBody TransferJobRequestDTO transferRequest) {
        return null;
    }

    @GetMapping
    public ResponseEntity<Collection<TransferJobRequestDTO>> listScheduledJobs(@RequestParam String userEmail) {
        return null;
    }

    @GetMapping("/details")
    public ResponseEntity<TransferJobRequestDTO> getScheduledJob(@RequestParam UUID jobUuid) {
        return null;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteScheduledJob(@RequestParam UUID jobUuid) {
        return null;
    }

    @PostMapping("/run")
    public ResponseEntity<UUID> runJob(@RequestBody TransferJobRequestDTO transferJobRequest){
        return null;
    }

}
