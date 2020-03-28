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

    @Data
    public static class RegistrationControllerRequest{
        private String email;
        private String code;
        private String firstName;
        private String lastName;
        private String organization;
        private String captchaVerificationValue;
    }
}

