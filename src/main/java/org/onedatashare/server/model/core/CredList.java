package org.onedatashare.server.model.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CredList {

    private List<String> list;

    public CredList(){
        this.list = new ArrayList<>();
    }

}
