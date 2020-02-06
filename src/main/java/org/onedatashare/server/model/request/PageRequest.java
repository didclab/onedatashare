package org.onedatashare.server.model.request;

import lombok.Data;

@Data
public class PageRequest {
    public int pageNo;
    public int pageSize;
    public String sortBy;
    public String sortOrder;
}
