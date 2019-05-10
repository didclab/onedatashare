package org.onedatashare.server.model.ticket;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Model for response from Freshdesk server. Constructed using ObjectMapper by converting JSON string response from Freshdesk.
 *
 * version 1.0 used Redmine
 * @version 2.0
 * @since 05-03-2019
 */
@Data
public class FreshdeskResponse{

    private String[] cc_emails;
    private String[] fwd_emails;
    private String[] reply_cc_emails;
    private String[] ticket_cc_emails;
    private Boolean fr_escalated;
    private Boolean spam;
    private Long email_config_id;
    private Long group_id;
    private Integer priority;
    private Long requester_id;
    private Long responder_id;
    private Integer source;
    private Long company_id;
    private Integer status;
    private String subject;
    private String[] to_emails;
    private Long product_id;
    private Integer id;    // ticket ID
    private String type;
    private Date due_by;
    private Date fr_due_by;
    private Boolean is_escalated;
    private String description;
    private String description_text;
    private Map<Object, Object> custom_fields;
    private Date created_at;
    private Date updated_at;
    private String[] tags;
    private String[] attachments;

}

/*
Sample JSON response from Freshdesk for issue creation :
{
    "cc_emails": [],
    "fwd_emails": [],
    "reply_cc_emails": [],
    "ticket_cc_emails": [],
    "fr_escalated": false,
    "spam": false,
    "email_config_id": null,
    "group_id": null,
    "priority": 2,
    "requester_id": 2043037446888,
    "responder_id": null,
    "source": 2,
    "company_id": null,
    "status": 2,
    "subject": "TEst ticket 2 using api",
    "to_emails": null,
    "product_id": null,
    "id": 10,
    "type": null,
    "due_by": "2019-05-08T20:00:00Z",
    "fr_due_by": "2019-05-08T19:00:00Z",
    "is_escalated": false,
    "description": "<div>TEst ticket <br> New Line</div>",
    "description_text": "TEst ticket   New Line",
    "custom_fields": {},
    "created_at": "2019-05-07T21:52:26Z",
    "updated_at": "2019-05-07T21:52:26Z",
    "tags": [],
    "attachments": []
}
 */
