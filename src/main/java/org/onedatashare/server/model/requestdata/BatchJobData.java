package org.onedatashare.server.model.requestdata;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
public class BatchJobData {

    private Long id;

    private Long version;

    private Long jobInstanceId;

    private Timestamp createTime;

    private Timestamp startTime;

    private Timestamp endTime;

    private String status;

    private String exitCode;

    private String exitMessage;

    private Timestamp lastUpdated;

    List<BatchStepExecution> batchSteps;

    Map<String,String> jobParameters;
}