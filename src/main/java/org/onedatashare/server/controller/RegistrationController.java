package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.UserRequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.onedatashare.server.model.core.ODSConstants.*;

@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = REGISTRATION_ENDPOINT, method = RequestMethod.POST)
    public Object register(@RequestBody UserRequestData userRequestData) {
        UserAction userAction = UserAction.convertToUserAction(userRequestData);
        return userService.register(userAction.getEmail(), userAction.getFirstName(), userAction.getLastName(),
                userAction.getOrganization(), userAction.getCaptchaVerificationValue());
    }

    @RequestMapping(value = VERIFICATION_ENDPOINT, method = RequestMethod.POST)
    public Object verifyEmail(@RequestBody UserRequestData userRequestData) {
        UserAction userAction = UserAction.convertToUserAction(userRequestData);
        return userService.verifyCode(userAction.getEmail(), userAction.getCode());
    }

//    @RequestMapping(value = SEND_VERIFICATION_ENDPOINT, method = RequestMethod.POST)
//    public Object sendVerificationCode(@RequestBody UserRequestData userRequestData) {
//        UserAction userAction = UserAction.convertToUserAction(userRequestData);
//        return userService.sendVerificationCode(userAction.getEmail(), TOKEN_TIMEOUT_IN_MINUTES);
//    }
}