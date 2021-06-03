/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.request.JobRequestData;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller that deletes a Job from the queue page
 */
@RestController
@RequestMapping("/api/stork/deleteJob")
public class DeleteJobController {

    /**
     *This will delete the user available history of their transfers that have happened.
     * We will probably have to expose a Monitoring service that would delete the data from CockroachDB or we could just offload that data to S3
     * Hiding it from the user and removing it from our DB.
     *
     * @param headers - Request header
     * @param jobRequestData - Data to perform an operation on the Job
     * @return a map containing all the endpoint credentials linked to the user account as a Mono
     */
    @PostMapping
    public Mono<Job> restartJob(@RequestHeader HttpHeaders headers, @RequestBody JobRequestData jobRequestData){
        return null;
    }
}