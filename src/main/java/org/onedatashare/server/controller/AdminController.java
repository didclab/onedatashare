package org.onedatashare.server.controller;

import org.onedatashare.server.model.useraction.notificationBody;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.AdminService;
import org.onedatashare.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.onedatashare.server.model.core.User;

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
    public Flux<Response> sendNotifications(@RequestHeader HttpHeaders headers, @RequestBody notificationBody body){
        for(String email:body.getEmailList()){
            try {
                String subject = body.getSubject();
                String emailText = body.getMessage();
                emailService.sendEmail(email, subject, emailText);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return Flux.error(new Exception("Email Sending Failed."));
            }
        }
        return Flux.just(new Response("Success", 200));
    }

}
