package org.onedatashare.server.model.core;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

@Data
public class JobStatistic {
    int jobId;
    Timestamp startTime;
    Timestamp endTime;
    Status status;
    Timestamp lastUpdated;
    Set<Integer> readCount;
    Set<Integer> writeCount;
    //    List<String> fileName;
    Map<String, String> strVal;
}
