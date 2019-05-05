package org.onedatashare.server.model.ticket;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Model holding the data for support ticket (data sent in HTTP request).
 *
 * @author Linus Castelino
 * @version 1.0
 * @since 05-03/2018
 */
@Data
@Component
public class SupportTicket {

    private final String REDMINE_PROJECT_ID = "ods";

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String issueDescription;

    /**
     * This method converts the data sent in a support ticket request into a string representation.
     * This string will be added to the description section of the ticket in Redmine.
     *
     * @return A string representation of the content
     */
    @Override
    public String toString(){
        return  "Name : " + this.getName() + "\\n" +
                "Email : " + this.getEmail() + "\\n" +
                "Phone : " + this.getPhone() + "\\n\\n" +
                "Issue Description : \\n" + this.getIssueDescription();
    }    // toString()

    /**
     * This method constructs the JSON request to be sent to Redmine server for this support ticket request.
     *
     * @return String representation of JSON body to be sent to Redmine server issue creation request.
     */
    public String getRequestString(){
        return "{" +
                    "\"issue\" : {" +
                        "\"project_id\" : \"" + getREDMINE_PROJECT_ID() + "\"," +
                        "\"subject\" : \"" + getSubject() + "\"," +
                        "\"description\" : \"" + this.toString() + "\"" +
                    "}" +
                "}";
    }    //getRequestString()
}    //class
