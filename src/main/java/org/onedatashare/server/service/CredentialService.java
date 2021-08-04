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

import org.onedatashare.server.model.core.CredList;
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.security.auth.login.CredentialNotFoundException;
import java.net.URI;

@Service
public class CredentialService {

    @Value("${cred.service.uri}")
    private String credentialServiceUrl;
    private String urlFormatted, credListUrl;
    private static Logger logger = LoggerFactory.getLogger(CredentialService.class);

    private static final int TIMEOUT_IN_MILLIS = 10000;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostConstruct
    private void initialize(){
        this.urlFormatted = this.credentialServiceUrl + "/%s/%s/%s";
        this.credListUrl = this.credentialServiceUrl + "/%s/%s";
    }

    private Mono<String> getUserId(){
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (String) securityContext.getAuthentication().getPrincipal());
    }

    private WebClient.ResponseSpec fetchCredential(String userId, EndpointType type, String credId){
        logger.info(String.format(this.urlFormatted, userId, type, credId));
        return this.webClientBuilder.build().get()
                .uri(URI.create(String.format(this.urlFormatted, userId, type, credId)))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new CredentialNotFoundException()))
                .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new Exception("Internal server error")));
    }

    public Mono<CredList> getStoredCredentialNames(String userId, EndpointType type){
        return this.webClientBuilder.build().get()
                .uri(URI.create(String.format(this.credListUrl, userId, type)))
                .retrieve()
                .bodyToMono(CredList.class);
    }

    public Mono<AccountEndpointCredential> fetchAccountCredential(EndpointType type, String credId){
        return getUserId()
                .flatMap(
                        userId -> fetchCredential(userId, type, credId)
                        .bodyToMono(AccountEndpointCredential.class)
                );
    }

    public Mono<HttpStatus> createCredential(AccountEndpointCredential credential, String userId, EndpointType type){
        return this.webClientBuilder.build().post()
                .uri(URI.create(String.format(this.urlFormatted, userId, "account-cred", type)))
                .body(BodyInserters.fromPublisher(Mono.just(credential), AccountEndpointCredential.class))
                .exchange()
                .map(response -> response.statusCode());
    }

    public Mono<Void> createCredential(OAuthEndpointCredential credential, String userId, EndpointType type){
        return this.webClientBuilder.build().post()
                .uri(URI.create(String.format(this.urlFormatted, userId, "oauth-cred" ,type)))
                .body(BodyInserters.fromPublisher(Mono.just(credential), OAuthEndpointCredential.class))
                .exchange()
                .then();
    }

    public Mono<OAuthEndpointCredential> fetchOAuthCredential(EndpointType type, String credId){
        return getUserId()
                .flatMap(
                        userId -> fetchCredential(userId, type, credId)
                                .bodyToMono(OAuthEndpointCredential.class)
                );
    }

    public Mono<Void> deleteCredential(String userId, EndpointType type, String credId) {
        return this.webClientBuilder.build().delete()
                .uri(URI.create(String.format(this.urlFormatted, userId, type, credId)))
                .exchange()
                .then();
    }
}
