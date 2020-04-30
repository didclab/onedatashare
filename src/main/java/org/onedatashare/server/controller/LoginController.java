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
import org.onedatashare.server.model.request.LoginControllerRequest;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.onedatashare.server.model.core.ODSConstants.*;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = AUTH_ENDPOINT, method = RequestMethod.POST)
    public Mono<ResponseEntity> login(@RequestBody LoginControllerRequest request) {
        return userService.login(request.getEmail(), request.getPassword())
                // Access token
                .map(loginResponse -> {
                    String cookieString = ResponseCookie.from(TOKEN_COOKIE_NAME, loginResponse.getToken())
                            .httpOnly(true)
                            .maxAge(Duration.ofSeconds(loginResponse.getExpiresIn()))
                            .build().toString();
                    HttpHeaders responseHeaders = new HttpHeaders();
                    //;SameSite=Strict;
                    responseHeaders.set(HttpHeaders.SET_COOKIE,
                            cookieString);
                    //Remove the token from the response
                    loginResponse.setToken(null);
                    return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
                });
    }


    /**
     * This function removes the ATOKEN cookie from the browser
     * @return Mono<ResponseEntity>
     */
    @RequestMapping(value = LOGOUT_ENDPOINT, method = RequestMethod.POST)
    public Mono<ResponseEntity> logout() {
        return Mono.fromSupplier(() -> {
            String cookieString = ResponseCookie.from(TOKEN_COOKIE_NAME, null)
                    .httpOnly(true)
                    .build().toString();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(HttpHeaders.SET_COOKIE,
                    cookieString + 	"; Max-Age=" + 0);
            return ResponseEntity.ok().headers(responseHeaders).body(null);
        });
    }

    @RequestMapping(value = IS_REGISTERED_EMAIL_ENDPOINT, method = RequestMethod.POST)
    public Mono<Boolean> isRegisteredEmail(@RequestBody LoginControllerRequest request){
        return userService.isRegisteredEmail(request.getEmail());
    }

    @RequestMapping(value = SEND_PASSWD_RST_CODE_ENDPOINT, method = RequestMethod.POST)
    public Mono<Response> sendPasswordResetCode(@RequestBody LoginControllerRequest request){
        return userService.sendVerificationCode(request.getEmail(), TOKEN_TIMEOUT_IN_MINUTES);
    }

    @RequestMapping(value = RESET_PASSWD_ENDPOINT, method = RequestMethod.POST)
    public Mono<Boolean> resetPassword(@RequestBody LoginControllerRequest request){
        return userService.resetPassword(request.getEmail(), request.getPassword(), request.getConfirmPassword(),
                request.getCode());
    }

    @RequestMapping(value = UPDATE_PASSWD_ENDPOINT, method = RequestMethod.POST)
    public Mono<String> updatePassword(@RequestBody LoginControllerRequest request){
        return userService.resetPasswordWithOld(null, request.getPassword(), request.getNewPassword(),
                request.getConfirmPassword());
    }
}
