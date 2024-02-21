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

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.onedatashare.server.model.core.CredList;
import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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

    private RestClient.Builder restClientBuilder;

    private EurekaClient discoveryClient;

    public CredentialService(EurekaClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
        this.discoveryClient = discoveryClient;
    }

    @PostConstruct
    private void initialize() {
        this.urlFormatted = this.credentialServiceUrl + "/%s/%s/%s";
        this.credListUrl = this.credentialServiceUrl + "/%s/%s";
    }

    private String getUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    //TODO: check exception handling
    private RestClient.ResponseSpec fetchCredential(String userId, EndpointType type, String credId) {
        return this.restClientBuilder.build().get()
                .uri(URI.create(String.format(this.urlFormatted, userId, type, credId)))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request,response) -> {
                    try {
                        throw new CredentialNotFoundException(response.getStatusText());
                    } catch (CredentialNotFoundException e) {
                        e.printStackTrace();
                    }
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request,response) -> {
                    try {
                        throw new Exception("Internal server error");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public CredList getStoredCredentialNames(String userId, EndpointType type) {
        switch (type) {
            case vfs:
                return this.getVfsNodesOfUserName(userId, type);
            default:
                return this.restClientBuilder.build().get()
                        .uri(URI.create(String.format(this.credListUrl, userId, type)))
                        .retrieve()
                        .body(CredList.class);
        }
    }

    /**
     * This Queries Eureka for all services that contain the current users username.
     * VFS nodes will require having the username set as an ENV property.
     * @param userId
     * @param type
     * @return
     */
    private CredList getVfsNodesOfUserName(String userId, EndpointType type) {
        CredList credList = new CredList();
        if (type.equals(EndpointType.vfs)) {
            for (Application application : this.discoveryClient.getApplications().getRegisteredApplications()) {
                String applicationName = application.getName();
                if (applicationName.toLowerCase().contains(userId.toLowerCase())) {
                    credList.getList().add(application.getName().toLowerCase());
                }
            }
        }
        return credList;
    }


    public AccountEndpointCredential fetchAccountCredential(EndpointType type, String credId) {
        return fetchCredential(getUserId(),type,credId).body(AccountEndpointCredential.class);
    }

    public HttpStatusCode createCredential(AccountEndpointCredential credential, String userId, EndpointType type) {
        return this.restClientBuilder.build().post()
                .uri(URI.create(String.format(this.urlFormatted, userId, "account-cred", type)))
                .body(credential)
                .exchange(((clientRequest, clientResponse) -> clientResponse.getStatusCode()));
    }

    public HttpStatusCode createCredential(OAuthEndpointCredential credential, String userId, EndpointType type) {
        return this.restClientBuilder.build().post()
                .uri(URI.create(String.format(this.urlFormatted, userId, "oauth-cred", type)))
                .body(credential)
                .exchange(((clientRequest, clientResponse) -> clientResponse.getStatusCode()));
    }

    public OAuthEndpointCredential fetchOAuthCredential(EndpointType type, String credId) {
        return fetchCredential(getUserId(),type,credId).body(OAuthEndpointCredential.class);
    }

    public HttpStatusCode deleteCredential(String userId, EndpointType type, String credId) {
        return this.restClientBuilder.build().delete()
                .uri(URI.create(String.format(this.urlFormatted, userId, type, credId)))
                .exchange((clientRequest, clientResponse) -> clientResponse.getStatusCode());
    }
}
