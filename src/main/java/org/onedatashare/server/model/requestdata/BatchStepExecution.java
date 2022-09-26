package org.onedatashare.server.model.requestdata;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BatchStepExecution {

    private Long id;

    private Long version;

    private String step_name;

    private Long jobInstanceId;

    private Timestamp startTime;

    private Timestamp endTime;

    private String status;

    private Long commitCount;

    private Long readCount;

    private Long filterCount;

    private Long writeCount;

    private Long readSkipcount;

    private Long writeSkipCount;

    private Long processSkipCount;

    private Long rollbackCount;

    private String exitCode;

    private String exitMessage;

    private Timestamp lastUpdated;

}
