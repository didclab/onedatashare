package org.onedatashare.server.controller;

import org.onedatashare.server.model.InitialAndFinal;
import org.onedatashare.server.model.carbon.CarbonMeasurement;
import org.onedatashare.server.service.TransferSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carbon")
public class CarbonController {

    private final TransferSchedulerService schedulerService;
    Logger logger = LoggerFactory.getLogger(CarbonController.class);

    public CarbonController(TransferSchedulerService transferSchedulerService) {
        this.schedulerService = transferSchedulerService;
    }

    @GetMapping("/query/{transferNodeName}/{jobUuid}")
    public ResponseEntity<List<CarbonMeasurement>> getJobAndNodeMeasurements(@PathVariable UUID jobUuid, @PathVariable String transferNodeName, Principal principal) {
        String odsUserEmail = principal.getName();
        return ResponseEntity.ok(this.schedulerService.getCarbonEntry(jobUuid, transferNodeName, odsUserEmail));
    }

    @GetMapping("/job/{jobUuid}")
    public ResponseEntity<List<CarbonMeasurement>> getAllCarbonIntensityForJob(@PathVariable UUID jobUuid, Principal principal) {
        return ResponseEntity.ok(this.schedulerService.getAllCarbonEntriesForJob(jobUuid));
    }

    @GetMapping("/latest/{jobUuid}")
    public ResponseEntity<CarbonMeasurement> getLatestCarbonIntensityForJob(@PathVariable UUID jobUuid, Principal principal) {
        return ResponseEntity.ok(this.schedulerService.getLatestCarbonEntryForJob(jobUuid));
    }

    @GetMapping("/user")
    public ResponseEntity<List<CarbonMeasurement>> getAllUserEntries(Principal principal) {
        return ResponseEntity.ok(this.schedulerService.getAllUserEntries(principal.getName()));
    }

    @GetMapping("/node/{transferNodeName}")
    public ResponseEntity<List<CarbonMeasurement>> getAllNodeEntries(@PathVariable String transferNodeName) {
        return ResponseEntity.ok(this.schedulerService.getAllCarbonEntriesForNode(transferNodeName));
    }

    @GetMapping("/result/{jobUuid}")
    public ResponseEntity<InitialAndFinal<CarbonMeasurement>> querySchedulerResult(@PathVariable UUID jobUuid) {
        return ResponseEntity.ok(this.schedulerService.queryResultMeasurements(jobUuid));
    }
}
