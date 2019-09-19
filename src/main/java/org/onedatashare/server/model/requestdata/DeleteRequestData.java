package org.onedatashare.server.model.requestdata;


import lombok.Data;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserActionCredential;

import java.util.ArrayList;

@Data
public class DeleteRequestData {
    private String uri;
    private String id;
    private String type;
    private UserActionCredential credential;
    private ArrayList<IdMap> map;

}
