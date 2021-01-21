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


package org.onedatashare.server.service;

import org.apache.http.entity.ContentType;
import org.onedatashare.server.model.error.CredentialNotFoundException;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.model.request.TransferJobRequestWithMetaData;
import org.onedatashare.server.model.response.TransferJobSubmittedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;

@Service
public class TransferJobService {

    @Value("${transfer.scheduler.uri}")
    private String transferSchedulerUri;

    @Autowired
    private FtpService ftpService;

    @Autowired
    private SftpService sftpService;

    private WebClient client;
    private static final Duration timeoutDuration = Duration.ofSeconds(10);

    @PostConstruct
    private void initialize(){
        this.client = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .build();
    }

    public Mono<TransferJobSubmittedResponse> submitTransferJobRequest(String ownerId, TransferJobRequest request){
         return Mono.just(TransferJobRequestWithMetaData.getTransferRequestWithMetaData(ownerId, request))
                 .flatMap(requestWithMetaData -> client.post()
                 .uri(URI.create(transferSchedulerUri))
                 .syncBody(requestWithMetaData)
                 .retrieve()
                 .onStatus(HttpStatus::is4xxClientError,
                         response -> Mono.error(new CredentialNotFoundException()))
                 .onStatus(HttpStatus::is5xxServerError,
                         response -> Mono.error(new Exception("Internal server error")))
                 .bodyToMono(TransferJobSubmittedResponse.class)
                 .timeout(timeoutDuration));

    }
}
