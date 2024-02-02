/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */


package org.onedatashare.server.service;

import org.apache.commons.lang.RandomStringUtils;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.error.InvalidFieldException;
import org.onedatashare.server.model.error.InvalidODSCredentialsException;
import org.onedatashare.server.model.error.OldPwdMatchingException;
import org.onedatashare.server.model.response.LoginResponse;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.onedatashare.server.model.core.ODSConstants.TOKEN_TIMEOUT_IN_MINUTES;

/**
 * Service class for all operations related to users' information.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private JWTUtil jwtUtil;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> createUser(User user) {
        user.setRegisterMoment(System.currentTimeMillis());
        return userRepository.insert(user);
    }

    public Mono<LoginResponse> login(String email, String password) {
        return getUser(User.normalizeEmail(email))
                .filter(usr -> usr.getHash().equals(usr.hash(password)))
                .switchIfEmpty(Mono.error(new InvalidODSCredentialsException("Invalid username or password")))
                .map(user -> LoginResponse.LoginResponseFromUser(user, jwtUtil.generateToken(user), JWTUtil.getExpirationTime()))
                .doOnSuccess(userLogin -> saveLastActivity(email, System.currentTimeMillis()).subscribe());
    }

    public Object register(String email, String firstName, String lastName, String organization, String captchaVerificationValue) {
        if (!emailService.isValidEmail(email)) {
            return Mono.error(new InvalidFieldException("Invalid Email id"));
        }
        return captchaService.verifyValue(captchaVerificationValue)
                .flatMap(captchaVerified -> {
                    if (captchaVerified) {
                        return doesUserExists(email).flatMap(user -> {

                            // This would be a same temporary password for each user while creating,
                            // once the user goes through the whole User creation workflow, he/she can change the password.
                            String password = User.salt(20);
                            if (user.getEmail() != null && user.getEmail().equals(email.toLowerCase())) {
                                ODSLoggerService.logWarning("User with email " + email + " already exists.");
                                if (!user.isValidated()) {
                                    return sendVerificationCode(email, TOKEN_TIMEOUT_IN_MINUTES);
                                } else {
                                    return Mono.just(new Response("Account already exists", 302));
                                }
                            }
                            return createUser(new User(email, firstName, lastName, organization, password))
                                    .flatMap(createdUser -> sendVerificationCode(createdUser.getEmail(), TOKEN_TIMEOUT_IN_MINUTES));
                        });
                    } else {
                        return Mono.error(new Exception("Captcha verification failed"));
                    }
                });
    }

    public Mono<Response> resendVerificationCode(String email) {
        return doesUserExists(email).flatMap(user -> {
            if (user.getEmail() == null) {
                return Mono.just(new Response("User not registered", 500));
            }
            if (!user.isValidated()) {
                return sendVerificationCode(email, TOKEN_TIMEOUT_IN_MINUTES);
            } else {
                return Mono.just(new Response("User account is already validated.", 500));
            }
        });
    }

    public Mono<User> doesUserExists(String email) {
        User user = new User();
        return userRepository.findById(email)
                .switchIfEmpty(Mono.just(user))
                .onErrorResume(
                        throwable -> throwable instanceof Exception,
                        throwable -> Mono.just(user));
    }

    public Mono<Boolean> resetPassword(String email, String password, String passwordConfirm, String authToken) {
        return getUser(email).flatMap(user -> {
            if (!password.equals(passwordConfirm)) {
                return Mono.error(new Exception("Password is not confirmed."));
            } else if (user.getAuthToken() == null) {
                return Mono.error(new Exception("Does not have Auth Token"));
            } else if (user.getAuthToken().equals(authToken)) {
                user.setPassword(password);
                // Setting the verification code to null while resetting the password.
                // This will allow the user to use the same verification code multiple times with in 24 hrs.
                user.setCode(null);
                user.setAuthToken(null);
                user.setValidated(true);
                userRepository.save(user).subscribe();
                return Mono.just(true);
            } else {
                return Mono.error(new Exception("Wrong Token"));
            }
        });
    }

    public Mono<String> resetPasswordWithOld(String cookie, String oldPassword, String newPassword, String confirmPassword) {
        return getLoggedInUser(cookie).flatMap(user -> {
            if (!newPassword.equals(confirmPassword)) {
                ODSLoggerService.logError("Passwords don't match.");
                return Mono.error(new OldPwdMatchingException("Passwords don't match."));
            } else if (!user.checkPassword(oldPassword)) {
                ODSLoggerService.logError("Old Password is incorrect.");
                return Mono.error(new OldPwdMatchingException("Old Password is incorrect."));
            } else {
                try {
                    user.setPassword(newPassword);
                    userRepository.save(user).subscribe();
                    ODSLoggerService.logInfo("Password reset for user " + user.getEmail() + " successful.");
                    return Mono.just(user.getHash());
                } catch (RuntimeException e) {
                    return Mono.error(new OldPwdMatchingException(e.getMessage()));
                }
            }
        });
    }

    public Mono<User> getUser(String email) {
        return userRepository.findById(email)
                .switchIfEmpty(Mono.error(new Exception("No User found with Id: " + email)));
    }


    public Mono<Response> sendVerificationCode(String email, int expire_in_minutes) {
        return getUser(email).flatMap(user -> {
            String code = RandomStringUtils.randomAlphanumeric(6);
            user.setVerifyCode(code, expire_in_minutes);
            userRepository.save(user).subscribe();
            try {
                String subject = "OneDataShare Authorization Code";
                String emailText = "The authorization code for your OneDataShare account is : " + code;
                emailService.sendEmail(email, subject, emailText);
            } catch (Exception ex) {
                ex.printStackTrace();
                return Mono.error(new Exception("Email Sending Failed."));
            }
            return Mono.just(new Response("Success", 200));
        });
    }


    public Mono<Boolean> validate(String email, String authToken) {
        return getUser(email).flatMap(user -> {
            if (user.isValidated()) {
                return Mono.error(new Exception("Already Validated"));
            } else if (user.getAuthToken() == null) {
                return Mono.error(new Exception("Did not have Auth Token"));
            } else if (user.getAuthToken().equals(authToken)) {
                user.setValidated(true);
                user.setAuthToken(null);
                userRepository.save(user).subscribe();
                return Mono.just(true);
            } else {
                return Mono.error(new Exception("Wrong Token"));
            }
        });
    }

    /**
     * @param email email of the user of the operation
     * @param code  code to verify
     * @return String Auth Token
     * @description verifycode will generation auth token if it is not already created.
     * Every auth token is available to use once.
     * If used auth token is set to null and reset next time user verify the code.
     * @author Yifuyin
     */

    public Mono<String> verifyCode(String email, String code) {
        return getUser(email).flatMap(user -> {
            User.VerifyCode expectedCode = user.getCode();
            if (expectedCode == null) {
                return Mono.error(new Exception("code not set"));
            } else if (expectedCode.expireDate.before(new Date())) {
                return Mono.error(new Exception("code expired"));
            } else if (expectedCode.code.equals(code)) {
                user.setAuthToken(code + User.salt(12));
                userRepository.save(user).subscribe();
                return Mono.just(user.getAuthToken());
            } else {
                return Mono.error(new Exception("Code not match"));
            }
        });
    }

    public Mono<Boolean> isRegisteredEmail(String email) {
        return userRepository.existsById(email);
    }

    /**
     * //TODO: remove this function
     * Modified the function to use the security context
     * Placeholder function that will be removed later
     *
     * @param cookie - Unused parameter (to be removed)
     * @return
     */
    public Mono<User> getLoggedInUser(String cookie) {
        return getLoggedInUser();
    }

    /**
     * Modified the function to use security context for logging in
     *
     * @return User : The current logged in user
     */
    public Mono<User> getLoggedInUser() {
        return getLoggedInUserEmail()
                .flatMap(this::getUser);
    }


    /**
     * This function returns the email id of the user that has made the request.
     * This information is retrieved from security context set using JWT
     *
     * @return email: Email id of the user making the request
     */
    public Mono<String> getLoggedInUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (String) securityContext.getAuthentication().getPrincipal());
    }


    public Mono<Void> saveLastActivity(String email, Long lastActivity) {
        return getUser(email).doOnSuccess(user -> {
            user.setLastActivity(lastActivity);
            userRepository.save(user).subscribe();
        }).then();
    }

}