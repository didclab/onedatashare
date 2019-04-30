package org.onedatashare.server.model.jobaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobRequest {
    public String status;
    public int pageNo;
    public int pageSize;
    public String sortBy;
    public String sortOrder;
}