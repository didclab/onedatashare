package org.onedatashare.server.controller;

import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import org.onedatashare.server.model.core.Mail;
import org.onedatashare.server.model.core.UserDetails;
import org.onedatashare.server.model.request.ChangeRoleRequest;
import org.onedatashare.server.model.request.PageRequest;
import org.onedatashare.server.model.useraction.NotificationBody;
import org.onedatashare.server.model.util.MailUUID;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.AdminService;
import org.onedatashare.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller to do the generic admin operations
 */
@RestController
@RequestMapping("/api/stork/admin")
public class AdminController {

    @Autowired
    public AdminService adminService;

    @Autowired
    private EmailService emailService;

    @PostMapping(value = "/get-users")
    public Mono<UserDetails> getUsersPaged(@RequestBody PageRequest pageRequest) {
        return adminService.getUsersPaged(pageRequest);
    }

    @PostMapping(value = "/get-admins")
    public Mono<UserDetails> getAdminsPaged(@RequestBody PageRequest pageRequest){
        return adminService.getAdminsPaged(pageRequest);
    }

    @GetMapping(value = "/getAllUsers")
    public Flux getAllUsers(){
        return adminService.getAllUsers();
    }

    @GetMapping(value = "/getMails")
    public Flux<Mail> getAllMails() {
        return adminService.getAllMails();
    }

    @GetMapping(value = "/getTrashMails")
    public Flux<Mail> getTrashMails() {
        return adminService.getTrashMails();
    }

    @PostMapping(value = "/deleteMail")
    public Mono<Response> deleteMail(@RequestBody MailUUID mailId) {
        return adminService.deleteMail(mailId.getMailUUID()).map((mail) -> {
            if (mail.getStatus() == "deleted") {
                return new Response("Success", 200);
            } else {
                return new Response("Error", 401);
            }
        });
    }


    //TODO: make asynchonous
    @PostMapping(value = "/sendNotifications")
    public Response sendNotifications(@RequestBody NotificationBody body) {
        // check whether the No of recipients is within the sending limit
        GetSendQuotaResult result = emailService.getSendQuota();
        if (result != null && ((result.getMax24HourSend() - result.getSentLast24Hours()) > body.getEmailList().size())) {
            // send email
            for (String email : body.getEmailList()) {
                try {
                    String subject = body.getSubject();
                    String emailText = body.getMessage();
                    emailService.sendEmail(email, subject, emailText);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Exception("Email Sending Failed.");
                }
            }
            Mail newMail = new Mail(body.getEmailList(), body.getSenderEmail(), body.getSubject(), body.getMessage(), body.isHtml(), "Sent");
            adminService.saveMail(newMail).subscribe();
            return new Response("Success", 200);
        } else {
            return new Response("Sending Limit exceeded", 401);
        }
    }

    @PreAuthorize("hasAnyAuthority('OWNER')")
    @PutMapping("/change-role")
    /**
     * Only owner perform role changes
     */
    public Mono<Response> changeRole(@RequestBody ChangeRoleRequest changeRoleRequest){
        return adminService.changeRole(changeRoleRequest.getEmail(), changeRoleRequest.isMakeAdmin());
    }

}
