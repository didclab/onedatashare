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

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.onedatashare.server.model.ticket.SupportTicketRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * SupportTicketService is a service class that accepts the captured request from SupportTicketController
 * and connects to Freshdesk to create a ticket, returning the generated ticker number.
 *
 * Reference used for creating POST request in Java - https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
 *
 * @version 1.0
 * @since 05-03-2019
 */
@Service
public class SupportTicketService {

    @Autowired
    EmailService emailService;

    @Autowired
    CaptchaService captchaService;

    @Value("${github.repo.uri}")
    String repositoryString;

    @Value("${github.token}")
    String githubToken;

    @Value("${github.organization.id}")
    String organizationId;

    private GHRepository repository;

//    @PostConstruct
//    public void postConstruct() throws IOException {
//        GitHub github = new GitHubBuilder()
//
//                .withOAuthToken(githubToken, organizationId)
//                .build();
//        this.repository = github.getRepository(repositoryString);
//    }

    public Mono<Long> createGitHubSuppTicket(SupportTicketRequest supportTicketRequest) {
        return captchaService.verifyValue(supportTicketRequest.getCaptchaVerificationValue())
                .flatMap(captchaVerified -> {
                    if (captchaVerified) {
                        try {
                            GHIssue issue = repository.createIssue("User: " + supportTicketRequest.getEmail() + " " + supportTicketRequest.getSubject())
                                    .body(supportTicketRequest.getDescription())
                                    .label("BUG Report")
                                    .create();
                            return Mono.just(issue.getId());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return Mono.just((long) -1);
                });
    }
}