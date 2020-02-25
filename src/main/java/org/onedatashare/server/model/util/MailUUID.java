package org.onedatashare.server.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MailUUID {

    @JsonProperty("mailId")
    private String mailUUID;
    // getter and setter

    public String getMailUUID() {
        return mailUUID;
    }

    public void setMailUUID(String mailUUID) {
        this.mailUUID = mailUUID;
    }
}
