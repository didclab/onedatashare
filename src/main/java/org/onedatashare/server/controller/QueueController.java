package org.onedatashare.server.controller;


import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/q")
public class QueueController {

  @Autowired
  private JobService jobService;

  private static final String EMAIL_PARAM = "email";
  private static final String HASH_PARAM = "hash";

  @PostMapping
  // Avoiding master update for Queue since presentation is next week
  // will require changes in app code
//<<<<<<< HEAD
//  public Mono<JobDetails> queue(@RequestHeader HttpHeaders headers, @RequestBody JobRequest jobDetails) {
//    String cookie = headers.getFirst("cookie");
//    return jobService.getJobsForUserOrAdmin(cookie, jobDetails)
//=======
  public Mono<List<Job>> queue(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
    String temp = headers.getFirst("cookie");


    if(temp == null){
      if(!userAction.getEmail().equalsIgnoreCase("") && !userAction.getPassword().equalsIgnoreCase("")){
        temp = EMAIL_PARAM + "=" + userAction.getEmail() + "; " +
                HASH_PARAM + "=" + userAction.getPassword();
      }
    }

    String cookie = temp;
    return jobService.getAllUndeletedJobsForUser(cookie)
            .subscribeOn(Schedulers.elastic());

  }
}
