package org.onedatashare.server.model.core;

import lombok.Data;

import java.util.Date;

@Data
public class JobStatistic {
    int jobId;
    Date startTime;
    Date endTime;
    Status status;
    Date lastUpdated;
}
