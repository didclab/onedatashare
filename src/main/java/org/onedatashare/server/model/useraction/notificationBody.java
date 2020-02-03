package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class notificationBody {
    private String subject;
    private String message;
    private String senderEmail;
    private boolean isHtml;
    private ArrayList<String> emailList;

    notificationBody(String senderEmail,String subject, String message,ArrayList<String> emailList,boolean isHtml){
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.message = message;
        this.emailList = emailList;
        this.isHtml = isHtml;

    }
}
