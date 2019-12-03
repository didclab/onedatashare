package org.onedatashare.server.model.core;

import com.google.api.client.util.DateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Model that holds transfer Mail information.
 * Represents the document that is eventually stored in the MongoDB Job collection.
 */

@Document
@Data
public class Mail {

    @Id
    private UUID uuid;

    public synchronized UUID uuid() {
        if (uuid == null)
            uuid = UUID.randomUUID();
        return uuid;
    }

    private String subject;

    private String message;

    private ArrayList<String> recipients;

    private Date sentDateTime;

    private boolean isHtml;

    private String sender;

    private String status;

    public Mail(ArrayList<String> emailList, String sender, String subject,String message, boolean isHtml,String status){
        this.sentDateTime = new Date();
        this.message= message;
        this.sender = sender;
        this.recipients = emailList;
        this.uuid = uuid();
        this.status = status;
        this.subject = subject;
        this.isHtml= isHtml;
    }

}
