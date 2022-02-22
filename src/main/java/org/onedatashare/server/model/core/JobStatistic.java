package org.onedatashare.server.model.core;

import java.sql.Timestamp;

public class JobStatistic {
    long jobId;
    Timestamp startTime;
    Timestamp endTime;
    Status status;
    Timestamp lastUpdated;
    int readCount;
    int writeCount;

}
