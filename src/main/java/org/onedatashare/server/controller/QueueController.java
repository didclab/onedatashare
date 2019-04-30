package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.jobaction.JobAction;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/stork/q")
public class QueueController {

  @Autowired
  private JobService jobService;

  @PostMapping
  public Mono<List<Job>> queue(@RequestHeader HttpHeaders headers, @RequestBody JobAction jobAction) {
    String cookie = headers.getFirst("cookie");
    return jobService.getJobsForUserOrAdmin(cookie,jobAction.status)
            .subscribeOn(Schedulers.elastic());  }
}
