package org.onedatashare.server.model.requestdata;

import lombok.Data;
import org.onedatashare.server.model.useraction.IdMap;

import java.util.ArrayList;

@Data
public class OperationRequestData extends RequestData{
    private ArrayList<IdMap> map;
}
