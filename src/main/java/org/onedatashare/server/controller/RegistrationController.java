package org.onedatashare.server.controller;

import lombok.Data;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.onedatashare.server.model.core.ODSConstants.*;

@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = REGISTRATION_ENDPOINT, method = RequestMethod.POST)
    public Object registerUser(@RequestBody RegistrationControllerRequest request) {
        return userService.register(request.getEmail(), request.getFirstName(), request.getLastName(),
                request.getOrganization(), request.getCaptchaVerificationValue());
    }

    @RequestMapping(value = EMAIL_VERIFICATION_ENDPOINT, method = RequestMethod.POST)
    public Mono<String> verifyEmailUsingCode(@RequestBody RegistrationControllerRequest request) {
        return userService.verifyCode(request.getEmail(), request.getCode());
    }

    @RequestMapping(value = RESEND_ACC_ACT_CODE_ENDPOINT, method = RequestMethod.POST)
    public Mono<Response> resendAccountActivationCode(@RequestBody RegistrationControllerRequest request){
        return userService.resendVerificationCode(request.getEmail());
    }

}

@Data
class RegistrationControllerRequest{
    private String email;
    private String code;
    private String firstName;
    private String lastName;
    private String organization;
    private String captchaVerificationValue;
}