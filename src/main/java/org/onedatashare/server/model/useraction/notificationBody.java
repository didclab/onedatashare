package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class notificationBody {
    private String subject;
    private String message;
    private ArrayList<String> emailList;

    notificationBody(String subject, String message,ArrayList<String> emailList){
        this.subject = subject;
        this.message = message;
        this.emailList = emailList;
    }
}
