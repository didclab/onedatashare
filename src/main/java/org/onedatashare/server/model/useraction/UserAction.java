package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.server.model.requestdata.*;

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
     * @param transferRequestData - data for making a transfer request
     * @return UserAction
     */
    public static UserAction convertToUserAction(TransferRequestData transferRequestData){
        UserAction ua = new UserAction();
        ua.setSrc(transferRequestData.getSrc());
        ua.setDest(transferRequestData.getDest());
        ua.setOptions(transferRequestData.getOptions());
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
        return ua;
    }

}
