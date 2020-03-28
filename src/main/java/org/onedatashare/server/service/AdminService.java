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

import org.onedatashare.server.model.core.Mail;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.core.UserDetails;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.model.request.PageRequest;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.repository.MailRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.onedatashare.server.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.PageRequest.of;

/**
 * Service which backs Admin controller
 */
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final MailRepository mailRepository;

    private static Pageable generatePageFromRequest(PageRequest request){
        Sort.Direction direction = request.sortOrder.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        request.pageNo = request.pageNo - 1 < 0 ? 0 : request.pageNo - 1;
        Pageable page = of(request.pageNo, request.pageSize, Sort.by(direction, request.sortBy));
        return page;
    }

    public AdminService(UserRepository userRepository, MailRepository mailRepository) {
        this.userRepository = userRepository;
        this.mailRepository = mailRepository;
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
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

    public Mono<UserDetails> getAdminsPaged(PageRequest pageRequest){
        Pageable pageable = generatePageFromRequest(pageRequest);
        Mono<List<User>> admins = userRepository.findAllAdministrators(pageable).collectList();
        Mono<Long> adminCount = userRepository.countAdministrators();

        return admins.zipWith(adminCount, UserDetails::new);
    }

    public Mono<UserDetails> getUsersPaged(PageRequest pageRequest) {
        Pageable pageable = generatePageFromRequest(pageRequest);
        Mono<List<User>> users = userRepository.findAllUsers(pageable).collectList();
        Mono<Long> userCount = userRepository.countUsers();

        return users.zipWith(userCount, UserDetails::new);
    }

    public Mono<Response> changeRole(final String email, final boolean admin) {
        return userRepository.findById(email)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .map(user -> {
                    if (admin) {
                        user.grantAdminRole();
                    } else {
                        user.removeAdminRole();
                    }
                    System.out.println(user.getRoles());
                    return user;
                })
                .flatMap(userRepository::save)
                .thenReturn(new Response("Success", 200))
                .onErrorReturn(new Response("Internal Server Error", 500));
    }
}
