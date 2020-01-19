package org.onedatashare.server.controller;


import org.onedatashare.server.model.response.LoginResponse;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.requestdata.UserRequestData;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = ODSConstants.AUTH_ENDPOINT, method = RequestMethod.POST)
    public Mono<LoginResponse> login(@RequestBody UserRequestData userRequestData) {
        return userService.login2(userRequestData.getEmail(), userRequestData.getPassword());
    }

}
