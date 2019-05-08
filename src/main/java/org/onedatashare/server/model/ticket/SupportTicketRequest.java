package org.onedatashare.server.model.ticket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Model holding the data for support ticket (data sent in HTTP request).
 *
 * API documentation - https://developers.freshdesk.com/api/
 *
 * @version 1.0
 * @since 05-03/2018
 */
@Data
@Component
public class SupportTicketRequest {

    @NonNull
    private String name;

    @NonNull
    private String email;

    private String phone = "";

    @NonNull
    private String subject;

    @NonNull
    private String description;

    private String captchaVerificationValue;

    // mandatory settings required by Freshdesk
    private Integer source = 2;    // 2 -> Portal, indicating it was created using our webapp
    private Integer priority = 2;    // 2 -> Medium priority
    private Integer status = 2;    // 2 -> Open status

    /**
     * This method converts the data sent in a support ticket request into a string representation.
     * This string will be added to the description section of the ticket in Freshdesk.
     *
     * @return A string representation of the content
     */
    @Override
    public String toString(){
        return  "Name : " + this.getName() + "<br />" +
                "Email : " + this.getEmail() + "<br />" +
                "Phone : " + this.getPhone() + "<br /><br />" +
                "Issue Description : <br />" + this.getDescription();
    }    // toString()

    /**
     * This method constructs the JSON request to be sent to Freshdesk for this support ticket request.
     *
     * @return String representation of JSON body to be sent to Freshdesk issue creation request.
     */
    public String getRequestString(){
        return "{" +
                    "\"name\" : \"" + this.getName() + "\"," +
                    "\"email\" : \"" + this.getEmail() + "\"," +
                    "\"phone\" : \"" + this.getPhone() + "\"," +
                    "\"subject\" : \"" + this.getSubject() + "\"," +
                    "\"description\" : \"" + this.getDescription() + "\"," +
                    "\"source\" : " + this.getSource() + "," +
                    "\"status\" : " + this.getStatus() + "," +
                    "\"priority\" : " + this.getPriority() +
                "}";


    }    //getRequestString()
}    //class

/*
Sample request object to Freshdesk
{
        "name":"Test User",
        "email":"linuscas@buffalo.edu",
        "phone": "2019524216",
        "source" : 2,
        "status" : 2,
        "priority" : 2,
        "description" : "TEst ticket <br /> New Line",
        "subject" : "TEst ticket 2 using api"
}
*/