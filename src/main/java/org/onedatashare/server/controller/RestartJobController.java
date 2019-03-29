package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/restart")
public class RestartJobController {

    @Autowired
    private ResourceServiceImpl resourceService;

    private static final String EMAIL_PARAM = "email";
    private static final String HASH_PARAM = "hash";

    @PostMapping
    public Mono<Job> restartJob(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction){
        String temp = headers.getFirst("cookie");

        if(temp == null){
            if(!userAction.getEmail().equalsIgnoreCase("") && !userAction.getPassword().equalsIgnoreCase("")){
                temp = EMAIL_PARAM + "=" + userAction.getEmail() + "; " +
                        HASH_PARAM + "=" + userAction.getPassword();
            }
        }
        String cookie = temp;

        //System.out.println("Cookie: "+cookie+"\n JobId: "+userAction.getJob_id());
        return resourceService.restartJob(cookie, userAction);
    }
}
