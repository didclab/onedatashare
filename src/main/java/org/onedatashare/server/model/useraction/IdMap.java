package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdMap {
    String id;
    String path;
}
