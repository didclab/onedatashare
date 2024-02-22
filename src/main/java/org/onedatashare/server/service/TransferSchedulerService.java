/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */


package org.onedatashare.server.service;

import org.onedatashare.server.model.ScheduledTransferJobRequest;
import org.onedatashare.server.model.TransferJobRequestDTO;
import org.onedatashare.server.model.TransferParams;
import org.onedatashare.server.exceptionHandler.error.CredentialNotFoundException;
import org.onedatashare.server.model.request.StopRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class TransferSchedulerService {

    @Value("${transfer.scheduler.service.uri}")
    private String transferQueueingServiceUri;
    private static Logger logger = LoggerFactory.getLogger(TransferSchedulerService.class);

    private RestClient.Builder restClientBuilder;


    public TransferSchedulerService(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }


    //TODO: Fix commented code
    public ResponseEntity<Void> stopTransferJob(StopRequest stopRequest) {
        return restClientBuilder.build().post()
                .uri(transferQueueingServiceUri + "/stopJob")
                .contentType(MediaType.APPLICATION_JSON)
                .body(stopRequest)
                .retrieve()
//                .onStatus(HttpStatusCode::isError,
//                        (request, response) -> {
//                    throw new Exception(response.toString());
//                })
//                .onStatus(HttpStatusCode::is4xxClientError,
//                        (request, response) -> {throw new CredentialNotFoundException()})
//                .onStatus(HttpStatusCode::is5xxServerError,
//                        (request,response) -> {throw new Exception("Internal server error")})
                .toBodilessEntity();
    }

    public UUID scheduleJob(TransferJobRequestDTO transferRequest) {
        logger.info(transferRequest.toString());
        return restClientBuilder.build()
                .post()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path("/job/schedule").queryParam("jobStartTime", transferRequest.getOptions().getScheduledTime()).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(transferRequest)
                .retrieve()
                .body(UUID.class);
    }

    public List<ScheduledTransferJobRequest> listScheduledJobs(String userEmail) {
        return restClientBuilder.build()
                .get()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path("/jobs").queryParam("userEmail", userEmail).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ScheduledTransferJobRequest>>() {
                });
    }

    public TransferJobRequestDTO getJobDetails(UUID jobUuid) {
        return this.restClientBuilder.build()
                .get()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path("/job/details").queryParam("jobUuid", jobUuid).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TransferJobRequestDTO.class);
    }

    public void deleteScheduledJob(UUID jobUuid) {
        this.restClientBuilder.build()
                .delete()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path("/job/delete").queryParam("jobUuid", jobUuid).build())
                .retrieve().toBodilessEntity();

    }

    public ResponseEntity<Void> changeParams(TransferParams transferParams) {
        return this.restClientBuilder.build()
                .put()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path("/apply/application/params").build())
                .body(transferParams)
                .retrieve().toBodilessEntity();
    }
}
