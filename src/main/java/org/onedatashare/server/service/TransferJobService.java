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
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.CredentialConstants;
import org.onedatashare.server.model.credential.EndpointCredential;
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
    @Value("${transfer.job.service.uri}")
    private String transferQueueingServiceUri;

    @Autowired
    private FtpService ftpService;

    @Autowired
    private SftpService sftpService;

    @Autowired
    private CredentialService credentialService;

    private WebClient client;
    private static final Duration timeoutDuration = Duration.ofSeconds(10);

    @PostConstruct
    private void initialize(){
        this.client = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .build();
    }

    private Mono<List<TransferJobRequest.EntityInfo>> updateSource(TransferJobRequest.Source source){
        switch (source.getType()){
            case s3: throw new RuntimeException("Not yet implemented");
            case gftp: throw new RuntimeException("Not yet supported");
            case dropbox:
            case gdrive:
            case box:
                break;
            case http:
                break;
            case sftp:
                return sftpService.listAllRecursively(source);
            case ftp:
                return ftpService.listAllRecursively(source);
        }
        return null;
    }

    public Mono<TransferJobSubmittedResponse> submitRequest(String ownerId, TransferJobRequest request){
        TransferJobRequest.Source source = request.getSource();
        return updateSource(source)
                .map(updatedInfoList -> {
                    TransferJobRequestWithMetaData requestWithMetaData =
                            TransferJobRequestWithMetaData.getTransferRequestWithMetaData(ownerId, request);
                    requestWithMetaData.getSource().setInfoList(new HashSet<>(updatedInfoList));
                    return requestWithMetaData;
                }).flatMap(requestWithMetaData -> client.post()
                        .uri(URI.create(transferQueueingServiceUri))
                        .syncBody(requestWithMetaData)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError,
                                response -> Mono.error(new CredentialNotFoundException()))
                        .onStatus(HttpStatus::is5xxServerError,
                                response -> Mono.error(new Exception("Internal server error")))
                        .bodyToMono(TransferJobSubmittedResponse.class)
                        .timeout(timeoutDuration));
    }

    public Mono<TransferJobSubmittedResponse> ensureJobReadyRequest(TransferJobRequestWithMetaData requestWithMetaData){
        TransferJobSubmittedResponse responseObj = new TransferJobSubmittedResponse();
        if(requestWithMetaData.getSourceCredential() == null){
            responseObj.setStatus(2);
            responseObj.setMessage("Encountered an error setting your source Endpoint Credential object please make sure it is added ");
            return Mono.just(responseObj);
        }else if(requestWithMetaData.getDestCredential() == null){
            responseObj.setStatus(2);
            responseObj.setMessage("There was an issue encountering your Destination credId specified");
            return Mono.just(responseObj);
        }else{
            return client.post().uri(URI.create(this.transferQueueingServiceUri))
                    .syncBody(requestWithMetaData)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError,
                            response -> Mono.error(new CredentialNotFoundException()))
                    .onStatus(HttpStatus::is5xxServerError,
                            response -> Mono.error(new Exception("Internal server error")))
                    .bodyToMono(TransferJobSubmittedResponse.class)
                    .timeout(timeoutDuration);
        }
    }

    public Mono<TransferJobSubmittedResponse> submitJob(String ownerId, TransferJobRequest request){
        TransferJobRequestWithMetaData requestWithMetaData = TransferJobRequestWithMetaData.getTransferRequestWithMetaData(ownerId, request);

        if(CredentialConstants.ACCOUNT_CRED_TYPE.contains(request.getSource().getType())){
            credentialService.fetchAccountCredential(request.getSource().getType(), request.getSource().getCredId())
                    .map(accountEndpointCredential -> {
                        requestWithMetaData.setSourceCredential(accountEndpointCredential);
                        return requestWithMetaData;
                    });
        }else if(CredentialConstants.OAUTH_CRED_TYPE.contains(request.getSource().getType())){
            credentialService.fetchOAuthCredential(request.getSource().getType(), request.getSource().getCredId())
                    .map(oAuthEndpointCredential -> {
                        requestWithMetaData.setSourceCredential(oAuthEndpointCredential);
                        return requestWithMetaData;
                    });
        }
        if(CredentialConstants.ACCOUNT_CRED_TYPE.contains(request.getDestination().getType())){
            credentialService.fetchAccountCredential(request.getDestination().getType(), request.getDestination().getCredId())
                    .map(accountEndpointCredential -> {
                        requestWithMetaData.setDestCredential(accountEndpointCredential);
                        return requestWithMetaData;
                    });
        }else if(CredentialConstants.OAUTH_CRED_TYPE.contains(request.getDestination().getType())){
            credentialService.fetchOAuthCredential(request.getDestination().getType(), request.getDestination().getCredId())
                    .map(oAuthEndpointCredential -> {
                        requestWithMetaData.setDestCredential(oAuthEndpointCredential);
                        return requestWithMetaData;
                    });
        }
        return ensureJobReadyRequest(requestWithMetaData);
    }
}
