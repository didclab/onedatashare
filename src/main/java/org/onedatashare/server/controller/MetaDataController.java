package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.BatchJobData;
import org.onedatashare.server.model.requestdata.InfluxData;
import org.onedatashare.server.model.requestdata.MonitorData;
import org.onedatashare.server.model.response.PageImplResponse;
import org.onedatashare.server.service.MetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetaDataController {

    @Autowired
    MetaDataService metaDataService;

    //CDB calls
    @GetMapping("/job")
    public BatchJobData getJobStatistic(Principal principal, @RequestParam Long jobId) {
        return metaDataService.getJobStat(jobId);
    }

    @GetMapping("/all/job/ids")
    public List<Long> getAllJobIds(Principal principal) {
        return metaDataService.getAllJobIds(principal.getName());
    }

    @GetMapping("/all/jobs")
    public List<BatchJobData> getAllJobStats(Principal principal) {
        return metaDataService.getAllStats(principal.getName());
    }

    @GetMapping("/all/page/jobs")
    public PageImplResponse<BatchJobData> getAllJobStats(Principal principal, Pageable pageable) {
        return metaDataService.getAllStats(principal.getName(), pageable);
    }


    @GetMapping("/job/date")
    public BatchJobData getJobByStartDate(Principal principal, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return metaDataService.getStatByDate(principal.getName(), date);
    }

    @GetMapping("/all/jobs/range")
    public List<BatchJobData> getJobsByDateRange(Principal principal,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return metaDataService.getStatsByDateRange(principal.getName(), start, end);
    }

    /**
     *
     * @param principal
     * @param status: Has required values of: ABANDONED, COMPLETED, FAILED, STARTED, STARTING, STOPPED, STOPPING, UNKNOWN
     * @return
     */
    @GetMapping("/job/status")
    public List<BatchJobData> queryJobsByStatus(Principal principal, @RequestParam String status){
        return metaDataService.queryRunningJobs(principal.getName(), status);
    }

    @GetMapping("/jobs/id/list")
    public List<BatchJobData> getJobsByListOfIds(Principal principal, @RequestParam(value = "jobId", required = false) List<Long> jobIds) {
        return metaDataService.getManyJobStats(principal.getName(), jobIds);
    }

    @GetMapping("/all/page/jobs/range")
    public Page<BatchJobData> getJobsByDateRange(Principal principal,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end, Pageable pageable) {
        return metaDataService.getStatsByDateRange(principal.getName(), start, end, pageable);
    }


    //Influx Calls
    @GetMapping("/measurements/job")
    public List<InfluxData> jobMeasurements(Principal principal, @RequestParam Long jobId) {
        return metaDataService.measurementsUserJob(principal.getName(), jobId);
    }

    @GetMapping("/measurements/user")
    public List<InfluxData> allUserMeasurements(Principal principal) {
        return metaDataService.userMeasurements(principal.getName());
    }

    @GetMapping("/measurements/range")
    public List<InfluxData> userRangeMeasurements(Principal principal, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return metaDataService.measurementsByRange(start, end, principal.getName());
    }

    @GetMapping("/measurements/monitor")
    public MonitorData monitorAJob(Principal principal, Long jobId) {
        return metaDataService.monitor(principal.getName(), jobId);
    }

    @GetMapping("/measurements/job/node")
    public List<InfluxData> queryMeasurementsByNode(Principal principal, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start, @RequestParam String appId, @RequestParam Long jobId){
        return metaDataService.getJobMeasurementsUniversal(principal.getName(), jobId, start, appId);
    }

}
