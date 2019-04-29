package org.onedatashare.server.model.pagination;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.module.globusapi.EndPoint;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationAction {
    public int pageNo;
    public int pageSize;
    public String sortBy;
    public String sortOrder;
}
