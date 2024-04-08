package org.onedatashare.server.controller;

import org.onedatashare.server.model.carbon.CarbonIpEntry;
import org.onedatashare.server.model.carbon.CarbonMeasureResponse;
import org.onedatashare.server.model.carbon.CarbonTraceRouteResponse;
import org.onedatashare.server.service.TransferSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController("/api/carbon")
public class CarbonController {

    private final TransferSchedulerService schedulerService;

    public CarbonController(TransferSchedulerService transferSchedulerService) {
        this.schedulerService = transferSchedulerService;
    }

    @GetMapping("/measure/traceroute")
    public ResponseEntity<List<CarbonIpEntry>> traceRouteCarbon(@RequestParam String transferNodeName, @RequestParam String sourceIp, @RequestParam String destinationIp, Principal principal) {
        return ResponseEntity.ok(this.schedulerService.traceRouteCarbon(transferNodeName, sourceIp, destinationIp));
    }

    @GetMapping("/job/carbon")
    public ResponseEntity<CarbonMeasureResponse> carbonJob(@RequestParam UUID jobUuid) {
        return ResponseEntity.ok(this.schedulerService.carbonMeasure(jobUuid));
    }

    @GetMapping("/job/carbon/traceroute")
    public ResponseEntity<CarbonTraceRouteResponse> carbonJobTraceRoute(@RequestParam UUID jobUuid) {
        return ResponseEntity.ok(this.schedulerService.carbonTraceRoute(jobUuid));
    }

}
