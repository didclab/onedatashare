package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSubmissionRequest {
    @JsonProperty("endpoint")
    String endpoint;
    @JsonProperty("DATA_TYPE")
    private String dataType;
    @JsonProperty("submission_id")
    private String submissionId;
    @JsonProperty("label")
    private String label;
    @JsonProperty("notify_on_succeeded")
    private Boolean notifyOnSucceeded;
    @JsonProperty("notify_on_failed")
    private Boolean notifyOnFailed;
    @JsonProperty("notify_on_inactive")
    private Boolean notifyOnInactive;
    @JsonProperty("skip_activation_check")
    private Boolean skipActivationCheck;
    @JsonProperty("deadline")
    private String deadline;
    @JsonProperty("recursive")
    private boolean recursive;
    @JsonProperty("source_endpoint")
    private String sourceEndpoint;
    @JsonProperty("destination_endpoint")
    private String destinationEndpoint;
    @JsonProperty("DATA")
    private List<TaskItem> data;
    @JsonProperty("encrypt_data")
    private Boolean encryptData;
    @JsonProperty("sync_level")
    private Integer syncLevel;
    @JsonProperty("verify_checksum")
    private Boolean verifyChecksum;
    @JsonProperty("preserve_timestamp")
    private Boolean preserveTimestamp;
    @JsonProperty("delete_destination_extra")
    private Boolean deleteDestinationExtra;
    @JsonProperty("ignore_missing")
    private Boolean ignoreMissing;
    @JsonProperty("interpret_globs")
    private Boolean interpretGlobs;
    @JsonProperty("recursive_symlinks")
    private String recursiveSymlinks;
    @JsonProperty("perf_cc")
    private Integer perfCC;
    @JsonProperty("perf_p")
    private Integer perfP;
    @JsonProperty("perf_pp")
    private Integer perfPP;
    @JsonProperty("perf_udt")
    private Boolean perfUDT;
}