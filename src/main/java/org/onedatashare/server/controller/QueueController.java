package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/q")
public class QueueController {

  @Autowired
  private JobService jobService;

  private static final String EMAIL_PARAM = "email";
  private static final String HASH_PARAM = "hash";

  @PostMapping
  public Mono<List<Job>> queue(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {
    String temp = headers.getFirst("cookie");

    //System.out.println("Temp:"+temp);

    if(temp == null){
      //System.out.println("Email: "+userAction.getEmail()+" Hash: "+userAction.getPassword());
      if(!userAction.getEmail().equalsIgnoreCase("") && !userAction.getPassword().equalsIgnoreCase("")){
        temp = EMAIL_PARAM + "=" + userAction.getEmail() + "; " +
                HASH_PARAM + "=" + userAction.getPassword();
      }
    }

    String cookie = temp;
    return jobService.getAllJobsForUser(cookie)
            .subscribeOn(Schedulers.elastic());
  }
}
