package org.onedatashare.server.controller;

import org.onedatashare.server.service.AdminService;
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

    @GetMapping(value = "/getAllUsers")
    public Flux<User> getAllUsers(@RequestHeader HttpHeaders headers){
        return adminService.getAllUsers();
    }

}
