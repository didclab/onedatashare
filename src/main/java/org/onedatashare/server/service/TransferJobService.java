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
import org.onedatashare.server.model.request.TransferJobRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class TransferJobService {
    @Value("${transfer.job.service.uri}")
    private String transferQueueingServiceUri;

    @Autowired
    private GDriveService gDriveService;

    @Autowired
    private DbxService dbxService;

    @Autowired
    private BoxService boxService;

    @Autowired
    private VfsService vfsService;

    private WebClient client;
    private static final int TIMEOUT_IN_MILLIS = 10000;

    @PostConstruct
    private void initialize(){
        this.client = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .build();
    }

    private Mono<TransferJobRequest.Source> updateSource(TransferJobRequest.Source source){
        switch (source.getType()){
            case s3: throw new RuntimeException("Not yet implemented");
            case gridftp: throw new RuntimeException("Not yet supported");
            case dropbox:
            case box:
            case gdrive:
            case sftp:
            case ftp:
            case http:
                return vfsService.listRecursive(source);
        }
        return null;
    }

    public Mono<Void> submitRequest(TransferJobRequest request){
        TransferJobRequest.Source source = request.getSource();
        return updateSource(source)
                .map(source1 -> {
                    request.setSource(source);
                    System.out.println(request);
                    return 5;
//                    return client.post()
//                            .uri(URI.create(transferQueueingServiceUri))
//                            .syncBody(request)
//                            .retrieve()
//                            .onStatus(HttpStatus::is4xxClientError,
//                                    response -> Mono.error(new CredentialNotFoundException()))
//                            .onStatus(HttpStatus::is5xxServerError,
//                                    response -> Mono.error(new Exception("Internal server error")));
                }).then();
    }
}
