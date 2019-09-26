package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stork/q")
public class QueueController {

  @Autowired
  private JobService jobService;

  @PostMapping
  public Mono<JobDetails> queue(@RequestHeader HttpHeaders headers, @RequestBody JobRequest jobDetails) {
    String cookie = headers.getFirst(ODSConstants.COOKIE);
    return jobService.getJobsForUserOrAdmin(cookie, jobDetails)
            .subscribeOn(Schedulers.elastic());
  }

  @PostMapping("/update")
  public Mono<List<Job>> update(@RequestHeader HttpHeaders headers, @RequestBody List<UUID> jobIds) {
    String cookie = headers.getFirst(ODSConstants.COOKIE);
    return jobService.getUpdates(cookie, jobIds)
            .subscribeOn(Schedulers.elastic());
  }
}
