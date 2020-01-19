package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.UserRequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.onedatashare.server.model.core.ODSConstants.REGISTRATION_ENDPOINT;

@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = REGISTRATION_ENDPOINT, method = RequestMethod.POST)
    public Object register(@RequestBody UserRequestData userRequestData){
        UserAction userAction = UserAction.convertToUserAction(userRequestData);
        return userService.register(userAction.getEmail(), userAction.getFirstName(), userAction.getLastName(),
                userAction.getOrganization(), userAction.getCaptchaVerificationValue());
    }
}
