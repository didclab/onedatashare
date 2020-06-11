package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Task {
    @JsonProperty("DATA_TYPE")
    private String dataType;
    @JsonProperty("task_id")
    private String taskId;
    @JsonProperty("type")
    private String type;
    @JsonProperty("status")
    private String status;
    @JsonProperty("label")
    private String label;
    @JsonProperty("owner_id")
    private String ownerId;
    @JsonProperty("request_time")
    private String requestTime;
    @JsonProperty("completion_time")
    private String completionTime;
    @JsonProperty("deadline")
    private String deadline;
    @JsonProperty("source_endpoint_id")
    private String sourceEndpointId;
    @JsonProperty("destination_endpoint_id")
    private String destinationEndpointId;
    @JsonProperty("files")
    private Integer files;
    @JsonProperty("directories")
    private Integer directories;
    @JsonProperty("bytes_transferred")
    private Integer bytes_transferred;
    @JsonProperty("is_paused")
    private String isPaused;
}
