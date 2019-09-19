package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.server.model.requestdata.*;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAction {
    private String action;
    private String email;
    private String firstName;
    private String lastName;
    private String organization;
    private String password;
    private String uri;
    private String id;
    private ArrayList<IdMap> map;
    private String type;
    private String uuid;
    private String code;
    private String confirmPassword;
    private String newPassword;
    private UserActionResource src;
    private UserActionResource dest;
    private UserActionCredential credential;
    private Integer job_id;

    private boolean saveOAuth;
    private String filter_fulltext;
    private EndPoint globusEndpoint;
    private String username;
    private boolean isAdmin;

    private int pageNo;
    private int pageSize;
    private String sortBy;
    private String sortOrder;

    private String portNumber;
    private String captchaVerificationValue;

    public static UserAction convertToUserAction(RequestData requestData){
        UserAction ua = new UserAction();
        ua.setUri(requestData.getUri());
        ua.setId(requestData.getId());
        ua.setPortNumber(requestData.getPortNumber());
        ua.setType(requestData.getType());
        ua.setCredential(requestData.getCredential());
        return ua;
    }

    public static UserAction convertToUserAction(OperationRequestData operationRequestData){
        UserAction ua = new UserAction();
        ua.setUri(operationRequestData.getUri());
        ua.setId(operationRequestData.getId());
        ua.setPortNumber(operationRequestData.getPortNumber());
        ua.setType(operationRequestData.getType());
        ua.setCredential(operationRequestData.getCredential());
        ua.setMap(operationRequestData.getMap());
        return ua;
    }

    public static UserAction convertToUserAction(JobRequestData jobRequestData){
        UserAction ua = new UserAction();
        ua.setJob_id(jobRequestData.getJob_id());
        return ua;
    }

    public static UserAction convertToUserAction(TransferRequestData transferRequestData){
        UserAction ua = new UserAction();
        ua.setSrc(transferRequestData.getSrc());
        ua.setDest(transferRequestData.getDest());
        return ua;
    }

    public static UserAction convertToUserAction(UserRequestData userRequestData){
        UserAction ua = new UserAction();
        ua.setAction(userRequestData.getAction());
        ua.setEmail(userRequestData.getEmail());
        ua.setFirstName(userRequestData.getFirstName());
        ua.setLastName(userRequestData.getLastName());
        ua.setOrganization(userRequestData.getOrganization());
        ua.setUri(userRequestData.getUri());
        ua.setUuid(userRequestData.getUuid());
        ua.setCode(userRequestData.getCode());
        ua.setConfirmPassword(userRequestData.getConfirmPassword());
        ua.setSaveOAuth(userRequestData.isSaveOAuth());
        ua.setAdmin(userRequestData.isAdmin());
        ua.setPageNo(userRequestData.getPageNo());
        ua.setSortBy(userRequestData.getSortBy());
        ua.setSortOrder(userRequestData.getSortOrder());
        ua.setCaptchaVerificationValue(userRequestData.getCaptchaVerificationValue());
        return ua;
    }

}
