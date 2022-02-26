package org.onedatashare.server.model.core;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class JobStatistic {
    long jobId;
    Timestamp startTime;
    Timestamp endTime;
    Status status;
    Timestamp lastUpdated;
    int readCount;
    int writeCount;

}
