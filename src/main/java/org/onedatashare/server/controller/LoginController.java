package org.onedatashare.server.controller;

import lombok.Data;
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

    /**
     * This function sets the ATOKEN cookie in the browser and sends Login response
     * @return Mono<ResponseEntity>
     */

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
                    responseHeaders.set(HttpHeaders.SET_COOKIE,
                            cookieString+ ";SameSite=Strict; Max-Age=" + loginResponse.getExpiresIn());
                    //Remove the token from the response
                    loginResponse.setToken("");
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

    @Data
    public static class LoginControllerRequest {
        private String email;
        private String password;
        private String confirmPassword;
        private String newPassword;
        private String code;
    }
}