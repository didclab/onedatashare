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

import org.onedatashare.server.model.InitialAndFinal;
import org.onedatashare.server.model.ScheduledTransferJobRequest;
import org.onedatashare.server.model.TransferJobRequestDTO;
import org.onedatashare.server.model.TransferParams;
import org.onedatashare.server.model.carbon.CarbonMeasurement;
import org.onedatashare.server.model.node.FileTransferNodeMetaData;
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

    public ResponseEntity<Void> stopTransferJob(UUID jobUuid) {
        String path = "/job/stop/%s".formatted(jobUuid);
        return restClientBuilder.build().delete()
                .uri(transferQueueingServiceUri + path)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        (request, response) -> logger.error("Exception occurred while trying to stop transfer job:{}", response.getStatusText()))
                .onStatus(HttpStatusCode::is4xxClientError,
                        (request, response) -> logger.error("Credentials not found for the client trying to stop transfer job:{}", response))
                .onStatus(HttpStatusCode::is5xxServerError,
                        (request, response) -> logger.error("Internal server error occurred while trying to stop transfer job:{}", response))
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

    public ScheduledTransferJobRequest getJobDetails(UUID jobUuid) {
        return this.restClientBuilder.build()
                .get()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path("/job/details").queryParam("jobUuid", jobUuid).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ScheduledTransferJobRequest.class);
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

    public List<FileTransferNodeMetaData> getUsersFileTransferNodes(String userName) {
        String path = String.format("/api/nodes/connectors");
        return this.restClientBuilder.build()
                .get()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).queryParam("user", userName).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FileTransferNodeMetaData>>() {
                });
    }

    public List<FileTransferNodeMetaData> getOdsNodes() {
        String path = "/api/nodes/ods";
        return this.restClientBuilder.build()
                .get()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FileTransferNodeMetaData>>() {
                });
    }

    public Integer totalConnectedFileTransferNodes() {
        String path = "/api/nodes/count";
        return this.restClientBuilder.build()
                .get()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).build())
                .retrieve()
                .body(Integer.class);
    }

    public List<CarbonMeasurement> getCarbonEntry(UUID jobUuid, String transferNodeName, String odsUserEmail) {
        String path = "/api/carbon/entry";
        return this.restClientBuilder.build()
                .get()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).queryParam("jobUuid", jobUuid.toString()).queryParam("transferNodeName", transferNodeName).queryParam("userEmail", odsUserEmail).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CarbonMeasurement>>() {});
    }

    public List<CarbonMeasurement> getAllCarbonEntriesForJob(UUID uuid) {
        String path = "/api/carbon/all/%s".formatted(uuid.toString());
        return this.restClientBuilder.build()
                .get()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CarbonMeasurement>>() {
                });
    }

    public CarbonMeasurement getLatestCarbonEntryForJob(UUID uuid) {
        String path = "/api/carbon/latest/%s".formatted(uuid.toString());
        return this.restClientBuilder.build()
                .get()
                .uri(transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CarbonMeasurement.class);
    }

    public List<CarbonMeasurement> getAllUserEntries(String userEmail) {
        String path = "/api/carbon/user";
        return this.restClientBuilder.build()
                .get()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).queryParam("userEmail", userEmail).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CarbonMeasurement>>() {
                });
    }

    public List<CarbonMeasurement> getAllCarbonEntriesForNode(String transferNodeName) {
        String path = "/api/carbon/node/%s".formatted(transferNodeName);
        return this.restClientBuilder.build()
                .get()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CarbonMeasurement>>() {});
    }

    public InitialAndFinal<CarbonMeasurement> queryResultMeasurements(UUID jobUuid) {
        String path = "/api/carbon/job/result/%s".formatted(jobUuid.toString());
        return this.restClientBuilder.build()
                .get()
                .uri(this.transferQueueingServiceUri, uriBuilder -> uriBuilder.path(path).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<InitialAndFinal<CarbonMeasurement>>() {});
    }
}
