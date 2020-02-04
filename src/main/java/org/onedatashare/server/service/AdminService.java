package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Mail;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.repository.MailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.onedatashare.server.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service which backs Admin controller
 */
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final MailRepository mailRepository;

    public AdminService(UserRepository userRepository, MailRepository mailRepository) {
        this.userRepository = userRepository;
        this.mailRepository = mailRepository;
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Flux<User> getAllAdmins() {
        return userRepository.getAllAdminIds();
    }

    public Mono<Mail> saveMail(Mail mail) {
        if (mail.getUuid() == null) {
            mail.uuid();
        }
        return mailRepository.save(mail);
    }

    public Mono<Mail> deleteMail(String id) {
        return mailRepository.findById(UUID.fromString(id)).map(mail -> {
            mail.setStatus("deleted");
            mailRepository.save(mail).subscribe();
            return mail;
        });
    }

    public Flux<Mail> getAllMails() {
        return mailRepository.findAll();
    }

    public Flux<Mail> getTrashMails() {
        return mailRepository.findAllDeleted();
    }

}
