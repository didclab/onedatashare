package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    @JsonProperty("DATA_TYPE")
    private String dataType;
    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("task_id")
    private String taskId;
    @JsonProperty("submission_id")
    private String submissionId;
    @JsonProperty("resource")
    private String resource;
    @JsonProperty("value")
    private String value;
}
