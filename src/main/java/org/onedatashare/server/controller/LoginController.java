package org.onedatashare.server.controller;


import org.onedatashare.server.model.Response.LoginResponse;
import org.onedatashare.server.model.requestdata.UserRequestData;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loginpoint")
public class LoginController {

    @Autowired
    UserService userService;

    @PostMapping
    public Mono<LoginResponse> login(@RequestBody UserRequestData userRequestData) {
        return userService.login2(userRequestData.getEmail(), userRequestData.getPassword());
    }

}
