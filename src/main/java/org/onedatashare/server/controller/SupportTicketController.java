package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.SupportTicket;
import org.onedatashare.server.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * This class creates a controller that accepts requests for creating support cases
 * and returns the generated ticket number in response.
 *
 * @author Linus Castelino
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
     * @param supportTicket - Object containing request values
     * @return ticketNumber - An integer value returned by Redmine server after generating the ticket
     */
    @PostMapping
    public Integer handle(@RequestBody SupportTicket supportTicket){
        return supportTicketService.createSupportTicket(supportTicket);
    }
}
