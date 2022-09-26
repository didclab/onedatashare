package org.onedatashare.server.model.response;

import lombok.Data;
import org.onedatashare.server.model.requestdata.BatchJobData;

import java.util.List;

@Data
public class BatchJobDataPageResponse {
    Integer page;
    Integer per_page;
    Integer total;
    Integer total_pages;
    List<BatchJobData> data;
}
