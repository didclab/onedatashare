package org.onedatashare.server.controller;

import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import org.onedatashare.server.model.useraction.notificationBody;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.AdminService;
import org.onedatashare.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.onedatashare.server.model.core.User;
import reactor.core.publisher.Mono;

/**
 Controller to do the generic admin operations
 */
@RestController
@RequestMapping("/api/stork/admin")
public class AdminController {

    @Autowired
    public AdminService adminService;

    @Autowired
    private EmailService emailService;

    @GetMapping(value = "/getAllUsers")
    public Flux<User> getAllUsers(@RequestHeader HttpHeaders headers){
        return adminService.getAllUsers();
    }

    @PostMapping(value = "/sendNotifications")
    public Mono<Response> sendNotifications(@RequestHeader HttpHeaders headers, @RequestBody notificationBody body){
        //check whether the requested user is admin
        return adminService.isAdmin(body.getSenderEmail()).map((isAdmin)-> {
            if(isAdmin){
                // check whether the No of recipients is within the sending limit
                GetSendQuotaResult result = emailService.getSendQuota();
               // System.out.println("Max 24 hrs send " +result.getMax24HourSend());
               // System.out.println("get sent last 24 hours " +result.getSentLast24Hours());
                if(result!=null && ((result.getMax24HourSend()- result.getSentLast24Hours()) > body.getEmailList().size())){
                    // send email
                    for(String email:body.getEmailList()){
                        try {
                            String subject = body.getSubject();
                            String emailText = body.getMessage();
                            emailService.sendEmail(email, subject, emailText);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            new Exception("Email Sending Failed.");
                        }
                    }
                    return new Response("Success", 200);
                }else{
                    return new Response("Sending Limit exceeded",401);
                }
            }else{
                new Response("User not authorized to make this call.", 403);
            }
            return null;
        });
    }

}
