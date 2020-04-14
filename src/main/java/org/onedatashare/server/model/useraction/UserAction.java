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


package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.server.model.request.*;
import org.onedatashare.server.model.requestdata.SSHCommandData;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAction {
    // For selecting action on the endpoint
    private String action;

    // For handling registration
    private String firstName;
    private String lastName;
    private String organization;
    private String captchaVerificationValue;

    // For handling endpoint login
    private String username;

    // User Admin Flag
    private boolean isAdmin;

    // For handling login and password change
    private String email;
    private String password;
    private String confirmPassword;
    private String newPassword;
    private String code;

    // User preferences
    private boolean saveOAuth;
    private boolean compactViewEnabled;

    private String uuid;

    // For handling endpoint list and file operations
    private String type;
    private String uri;
    private String id;
    private String portNumber;
    private UserActionCredential credential;
    private ArrayList<IdMap> map;

    // For handling transfers
    private UserActionResource src;
    private UserActionResource dest;
    private TransferOptions options;

    // For queue page
    private Integer job_id;
    private int pageNo;
    private int pageSize;
    private String sortBy;
    private String sortOrder;

    // Misc
    private String filter_fulltext;
    private EndPoint globusEndpoint;

    /**
     * Factory method for returning an object of type request data
     * @param requestData - data for making a request on the endpoints
     * @return UserAction
     */
    public static UserAction convertToUserAction(RequestData requestData){
        UserAction ua = new UserAction();
        ua.setType(requestData.getType());
        ua.setUri(requestData.getUri());
        ua.setId(requestData.getId());
        ua.setPortNumber(requestData.getPortNumber());
        ua.setCredential(requestData.getCredential());
        return ua;
    }

    /**
     * Factory method for returning an object of type request data
     * @param operationRequestData - data for performing an operation on the endpoints
     * @return UserAction
     */
    public static UserAction convertToUserAction(OperationRequestData operationRequestData){
        UserAction ua = new UserAction();
        ua.setType(operationRequestData.getType());
        ua.setUri(operationRequestData.getUri());
        ua.setId(operationRequestData.getId());
        ua.setPortNumber(operationRequestData.getPortNumber());
        ua.setCredential(operationRequestData.getCredential());
        ua.setMap(operationRequestData.getMap());
        return ua;
    }

    /**
     * Factory method for returning an object of type request data
     * @param jobRequestData - data for making a job request
     * @return UserAction
     */
    public static UserAction convertToUserAction(JobRequestData jobRequestData){
        UserAction ua = new UserAction();
        ua.setJob_id(jobRequestData.getJob_id());
        return ua;
    }

    /**
     * Factory method for returning an object of type request data
     * @param transferRequest - data for making a transfer request
     * @return UserAction
     */
    public static UserAction convertToUserAction(TransferRequest transferRequest){
        UserAction ua = new UserAction();
        ua.setSrc(transferRequest.getSrc());
        ua.setDest(transferRequest.getDest());
        ua.setOptions(transferRequest.getOptions());
        return ua;
    }

    /**
     * Factory method for returning an object of type request data
     * @param userRequestData - data for making a user request
     * @return UserAction
     */
    public static UserAction convertToUserAction(UserRequestData userRequestData){
        UserAction ua = new UserAction();
        ua.setAction(userRequestData.getAction());

        ua.setEmail(userRequestData.getEmail());
        ua.setPassword(userRequestData.getPassword());
        ua.setConfirmPassword(userRequestData.getConfirmPassword());
        ua.setNewPassword(userRequestData.getNewPassword());

        ua.setFirstName(userRequestData.getFirstName());
        ua.setLastName(userRequestData.getLastName());
        ua.setOrganization(userRequestData.getOrganization());
        ua.setUri(userRequestData.getUri());
        ua.setUuid(userRequestData.getUuid());
        ua.setCode(userRequestData.getCode());
        ua.setSaveOAuth(userRequestData.isSaveOAuth());
        ua.setAdmin(userRequestData.isAdmin());
        ua.setPageNo(userRequestData.getPageNo());
        ua.setPageSize(userRequestData.getPageSize());
        ua.setSortBy(userRequestData.getSortBy());
        ua.setSortOrder(userRequestData.getSortOrder());
        ua.setCaptchaVerificationValue(userRequestData.getCaptchaVerificationValue());
        ua.setCompactViewEnabled(userRequestData.isCompactViewEnabled());
        return ua;
    }

    /**
     * Factory method that converts the SSH command request data to UserAction
     * @param commandData - SSH command request data
     * @return ua - UserAction object containing data received in the request
     */
    public static UserAction convertToUserAction(SSHCommandData commandData){
        UserAction ua = new UserAction();
        ua.setUri(commandData.getHost());
        ua.setCredential(commandData.getCredential());
        ua.setPortNumber(commandData.getPort());
        return ua;
    }

}
