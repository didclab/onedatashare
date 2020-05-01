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


package org.onedatashare.server.system.base;

import org.onedatashare.server.controller.RegistrationController.RegistrationControllerRequest;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.request.LoginControllerRequest;

/**
 * A base that holds common user actions
 */
public abstract class UserActionTest extends SystemTest {

    protected void resetUserPassword(String userEmail, String oldPassword, String newPassword) throws Exception {
        LoginControllerRequest requestData = new LoginControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setPassword(oldPassword);
        requestData.setNewPassword(newPassword);
        requestData.setConfirmPassword(newPassword);
        processPostWithRequestData(ODSConstants.UPDATE_PASSWD_ENDPOINT, requestData);
    }

    protected void setUserPassword(String userEmail, String userPassword, String authToken) throws Exception {
        LoginControllerRequest requestData = new LoginControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setPassword(userPassword);
        requestData.setConfirmPassword(userPassword);
        requestData.setCode(authToken);
        processPostWithRequestData(ODSConstants.RESET_PASSWD_ENDPOINT, requestData);
    }

    protected boolean loginUser(String userEmail, String password) throws Exception {
        LoginControllerRequest requestData = new LoginControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setPassword(password);
        long timeBeforeLoggingIn = System.currentTimeMillis();
        processPostWithRequestData(ODSConstants.AUTH_ENDPOINT, requestData);
        Long lastActivity = users.get(userEmail).getLastActivity();
        return lastActivity != null && lastActivity > timeBeforeLoggingIn;
    }

    protected void resendVerificationCode(String userEmail) throws Exception {
        RegistrationControllerRequest request = new RegistrationControllerRequest();
        request.setEmail(userEmail);
        processPostWithRequestData(ODSConstants.RESEND_ACC_ACT_CODE_ENDPOINT, request);
    }

    protected void registerUserAndChangePassword(String userEmail, String userPassword, String username) throws Exception {
        registerUser(userEmail, username);
        String verificationCodeEmail = userInbox.get(userEmail);
        String verificationCode = extractVerificationCode(verificationCodeEmail);
        verifyCode(userEmail, verificationCode);
        String authToken = users.get(userEmail).getAuthToken();
        setUserPassword(userEmail, userPassword, authToken);
    }

    protected void verifyCode(String userEmail, String verificationCode) throws Exception {
        RegistrationControllerRequest requestData = new RegistrationControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setCode(verificationCode);
        processPostWithRequestData(ODSConstants.EMAIL_VERIFICATION_ENDPOINT, requestData);
    }

    protected void registerUser(String userEmail, String firstName) throws Exception {
        RegistrationControllerRequest requestData = new RegistrationControllerRequest();
        requestData.setFirstName(firstName);
        requestData.setEmail(userEmail);
        processPostWithRequestData(ODSConstants.REGISTRATION_ENDPOINT, requestData);
    }

    protected String extractVerificationCode(String verificationCodeEmail) {
        return verificationCodeEmail.substring(verificationCodeEmail.lastIndexOf(" ")).trim();
    }
}
