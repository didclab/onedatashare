package org.onedatashare.server.controller;

import org.onedatashare.server.model.ticket.SupportTicketRequest;
import org.onedatashare.server.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
@RequestMapping(value = "/api/stork/ticket")
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
        return supportTicketService.createSupportTicket(supportTicketRequest);
    }
}
