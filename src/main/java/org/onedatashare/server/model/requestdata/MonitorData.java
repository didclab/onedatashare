package org.onedatashare.server.model.requestdata;

import lombok.Data;

import java.util.List;

@Data
public class MonitorData {
    List<InfluxData> measurements;
    BatchJobData batchJobData;
}
