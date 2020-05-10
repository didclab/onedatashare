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
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.security.auth.login.CredentialNotFoundException;
import java.net.URI;

@Service
public class CredentialService {
    private WebClient client;

    @Value("${cred.service.uri}")
    private String credentialServiceUrl;

    private static final int TIMEOUT_IN_MILLIS = 10000;

    @PostConstruct
    private void initialize(){
        this.client = WebClient.builder()
                .baseUrl(credentialServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .build();
    }

    private WebClient.ResponseSpec fetchCredential(String accessToken, EndpointType type, String credId){
        return client.get()
                .uri(URI.create(String.format("%s/%s/%s",credentialServiceUrl, type, credId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new CredentialNotFoundException()))
                .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new Exception("Internal server error")));
    }

    public Mono<AccountEndpointCredential> fetchAccountCredential(String accessToken, EndpointType type, String credId){
        return fetchCredential(accessToken, type, credId)
                .bodyToMono(AccountEndpointCredential.class);
    }

    public Mono<OAuthEndpointCredential> fetchOAuthCredential(String accessToken, EndpointType type, String credId){
        return fetchCredential(accessToken, type, credId)
                .bodyToMono(OAuthEndpointCredential.class);
    }
}