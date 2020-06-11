package org.onedatashare.server.model.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CredList {
    private List<String> list;
}
