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

import org.onedatashare.server.model.ticket.SupportTicketRequest;
import org.onedatashare.server.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This class creates a controller that accepts requests for creating support cases
 * and returns the generated ticket number in response.
 *
 * The support ticket functionality could be implemented on the UI directly, however that would exposed the
 * account API key. Support ticket requests are therefore routed through the backend and submitted to Freshdesk.
 *
 * @version 1.0
 * @since 05-03-2019
 */
@RestController
@RequestMapping(value = "/api/v1/stork/ticket")
public class SupportTicketController {

    @Autowired
    SupportTicketService supportTicketService;

    /**
     * This method handles requests for creating support tickets and returns the generated ticket number.
     * The request is served by SupportTicketService.
     *
     * Note - Unlike other controllers, this controller does not accept HttpHeaders as an argument for checking for cookies,
     * since a user can create a ticket even without logging in.
     *
     * @param supportTicketRequest - Object containing request values
     * @return ticketNumber - An integer value returned by Freshdesk after generating the ticket
     */
    @PostMapping
    public Mono<Integer> handle(@RequestBody SupportTicketRequest supportTicketRequest){
        return supportTicketService.createSupportTicket(supportTicketRequest).subscribeOn(Schedulers.elastic());
    }
}
