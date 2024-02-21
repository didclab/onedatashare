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
import org.onedatashare.server.exceptionHandler.error.InvalidFieldException;
import org.onedatashare.server.exceptionHandler.error.InvalidODSCredentialsException;
import org.onedatashare.server.exceptionHandler.error.ODSException;
import org.onedatashare.server.exceptionHandler.error.OldPwdMatchingException;
import org.onedatashare.server.model.response.LoginResponse;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;

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

    public User createUser(User user) {
        user.setRegisterMoment(System.currentTimeMillis());
        return userRepository.insert(user);
    }

    //TODO: validate
    public LoginResponse login(String email, String password) throws Exception {
        User user= getUser(User.normalizeEmail(email));
        if(!user.getHash().equals(user.hash(password))){
            throw new InvalidODSCredentialsException("Invalid username or password");
        }
        LoginResponse response=LoginResponse.LoginResponseFromUser(user, jwtUtil.generateToken(user), JWTUtil.getExpirationTime());
        saveLastActivity(email, System.currentTimeMillis());
        return response;
    }

    public Object register(String email, String firstName, String lastName, String organization, String captchaVerificationValue) throws Exception {
        if (!emailService.isValidEmail(email)) {
            throw new InvalidFieldException("Invalid Email id");
        }
        boolean captchaVerified= captchaService.verifyValue(captchaVerificationValue);
        if (captchaVerified) {
            User user=doesUserExists(email);
            // This would be a same temporary password for each user while creating,
            // once the user goes through the whole User creation workflow, he/she can change the password.
            String password = User.salt(20);
            if (user.getEmail() != null && user.getEmail().equals(email.toLowerCase())) {
                ODSLoggerService.logWarning("User with email " + email + " already exists.");
                if (!user.isValidated()) {
                    return sendVerificationCode(email, TOKEN_TIMEOUT_IN_MINUTES);
                } else {
                    return new Response("Account already exists", 302);
                }
            }
            User createdUser= createUser(new User(email, firstName, lastName, organization, password));
            return sendVerificationCode(createdUser.getEmail(), TOKEN_TIMEOUT_IN_MINUTES);
        } else {
            throw new Exception("Captcha verification failed");
        }
    }

    public Response resendVerificationCode(String email) throws Exception {
        User user= doesUserExists(email);
        if (user.getEmail() == null) {
            return new Response("User not registered", 500);
        }
        if (!user.isValidated()) {
            return sendVerificationCode(email, TOKEN_TIMEOUT_IN_MINUTES);
        } else {
            return new Response("User account is already validated.", 500);
        }
    }

    public User doesUserExists(String email) {
        User user=new User();
        return userRepository.findById(email).orElse(user);
    }

    public Boolean resetPassword(String email, String password, String passwordConfirm, String authToken) throws Exception {
        User user= getUser(email);
        if (!password.equals(passwordConfirm)) {
            throw new Exception("Password is not confirmed.");
        } else if (user.getAuthToken() == null) {
            throw new Exception("Does not have Auth Token");
        } else if (user.getAuthToken().equals(authToken)) {
            user.setPassword(password);
            // Setting the verification code to null while resetting the password.
            // This will allow the user to use the same verification code multiple times with in 24 hrs.
            user.setCode(null);
            user.setAuthToken(null);
            user.setValidated(true);
            userRepository.save(user);
            return true;
        } else {
            throw new Exception("Wrong Token");
        }
    }

    public String resetPasswordWithOld(String cookie, String oldPassword, String newPassword, String confirmPassword) throws Exception {
     User user=getLoggedInUser(cookie);
        if (!newPassword.equals(confirmPassword)) {
            ODSLoggerService.logError("Passwords don't match.");
           throw new OldPwdMatchingException("Passwords don't match.");
        } else if (!user.checkPassword(oldPassword)) {
            ODSLoggerService.logError("Old Password is incorrect.");
            throw new OldPwdMatchingException("Old Password is incorrect.");
        } else {
            try {
                user.setPassword(newPassword);
                userRepository.save(user);
                ODSLoggerService.logInfo("Password reset for user " + user.getEmail() + " successful.");
                return user.getHash();
            } catch (RuntimeException e) {
                throw new OldPwdMatchingException(e.getMessage());
            }
        }
    }

    public User getUser(String email) throws Exception {
        Optional<User> response= userRepository.findById(email);
        if(response.isPresent()){
            return response.get();
        }
        throw new Exception("No User found with Id: " + email);
    }


    public Response sendVerificationCode(String email, int expire_in_minutes) throws Exception {
        User user=getUser(email);
        String code = RandomStringUtils.randomAlphanumeric(6);
        user.setVerifyCode(code, expire_in_minutes);
        userRepository.save(user);
        try {
            String subject = "OneDataShare Authorization Code";
            String emailText = "The authorization code for your OneDataShare account is : " + code;
            emailService.sendEmail(email, subject, emailText);
        } catch (Exception ex) {
            throw new ODSException("Email Sending Failed.",ex.getClass().getName());
        }
        return new Response("Success", 200);
    }


    //TODO: Exception handling
    public Boolean validate(String email, String authToken) throws Exception {
        User user= getUser(email);
        if (user.isValidated()) {
            throw new Exception("Already Validated");
        } else if (user.getAuthToken() == null) {
            throw new Exception("Did not have Auth Token");
        } else if (user.getAuthToken().equals(authToken)) {
            user.setValidated(true);
            user.setAuthToken(null);
            userRepository.save(user);
            return true;
        } else {
            throw new Exception("Wrong Token");
        }
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

    //TODO: Exception handling
    public String verifyCode(String email, String code) throws Exception {
        User user= getUser(email);
        User.VerifyCode expectedCode = user.getCode();
        if (expectedCode == null) {
            throw new ODSException("code not set","VerificationCodeNotSet");
        } else if (expectedCode.expireDate.before(new Date())) {
            throw new ODSException("code expired","ExpiredCode");
        } else if (expectedCode.code.equals(code)) {
            user.setAuthToken(code + User.salt(12));
            userRepository.save(user);
            return user.getAuthToken();
        } else {
            throw new ODSException("Code not match","CodeNotMatched");
        }
    }

    public Boolean isRegisteredEmail(String email) {
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
    public User getLoggedInUser(String cookie) throws Exception {
        return getLoggedInUser();
    }

    /**
     * Modified the function to use security context for logging in
     *
     * @return User : The current logged in user
     */
    public User getLoggedInUser() throws Exception {
        return getUser(getLoggedInUserEmail());
    }


    /**
     * This function returns the email id of the user that has made the request.
     * This information is retrieved from security context set using JWT
     *
     * @return email: Email id of the user making the request
     */
    public String getLoggedInUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    public void saveLastActivity(String email, Long lastActivity) throws Exception {
        User user=getUser(email);
        user.setLastActivity(lastActivity);
        userRepository.save(user);
    }

}