package org.onedatashare.server.model.ticket;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Model for response from Redmine server. Constructed using ObjectMapper by converting JSON string response from Redmine.
 *
 * @version 1.0
 * @since 05-03-2019
 */
@Data
public class RedmineResponse {

    private Issue issue;

    public Integer getTicketId(){
        return getIssue().getId();
    }

    public String getIssueDescription(){
        return getIssue().getDescription();
    }

}    //class

@Data
class Issue{
    private Integer id;

    private IssueFieldObject project;
    private IssueFieldObject tracker;
    private IssueFieldObject status;
    private IssueFieldObject priority;
    private IssueFieldObject author;

    private String subject;
    private String description;
    private String start_date;
    private String due_date;
    private String done_ratio;
    private Boolean is_private;
    private String estimated_hours;
    private String total_estimated_hours;
    private String created_on;
    private String updated_on;
    private String closed_on;
}    //class

@Data
class IssueFieldObject{
    private Integer id;
    private String name;
}

/*
Sample JSON response from Redmine for issue creation :
{
    "issue": {
        "id": 6,
        "project": {
            "id": 2,
            "name": "test"
        },
        "tracker": {
            "id": 1,
            "name": "Bug"
        },
        "status": {
            "id": 1,
            "name": "New"
        },
        "priority": {
            "id": 2,
            "name": "Normal"
        },
        "author": {
            "id": 1,
            "name": "Linus Castelino Admin"
        },
        "subject": "test issue",
        "description": "TEst description",
        "start_date": "2019-05-04",
        "due_date": null,
        "done_ratio": 0,
        "is_private": false,
        "estimated_hours": null,
        "total_estimated_hours": null,
        "created_on": "2019-05-04T22:08:07Z",
        "updated_on": "2019-05-04T22:08:07Z",
        "closed_on": null
    }
}
 */