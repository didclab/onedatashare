package org.onedatashare.server.model.core;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SupportTicket {

    private final String REDMINE_PROJECT_ID = "ods";

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String issueDescription;

    @Override
    public String toString(){
        return  "Name : " + this.getName() + "\\n" +
                "Email : " + this.getEmail() + "\\n" +
                "Phone : " + this.getPhone() + "\\n\\n" +
                "Issue Description : \\n" + this.getIssueDescription();
    }

    public String getRequestString(){
        return "{" +
                    "\"issue\" : {" +
                        "\"project_id\" : \"" + getREDMINE_PROJECT_ID() + "\"," +
                        "\"subject\" : \"" + getSubject() + "\"," +
                        "\"description\" : \"" + this.toString() + "\"" +
                    "}" +
                "}";
    }
}
