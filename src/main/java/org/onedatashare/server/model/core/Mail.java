/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.model.core;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public Mail() {

    }

    public Mail(ArrayList<String> emailList, String sender, String subject,String message, boolean isHtml,String status){
        this.sentDateTime = new Date();
        this.message= message;
        this.sender = sender;
        this.recipients = emailList;
        this.status = status;
        this.subject = subject;
        this.isHtml= isHtml;
    }

}
