package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskItem {
    @JsonProperty("DATA_TYPE")
    private String dataType;
    @JsonProperty("source_path")
    private String sourcePath;
    @JsonProperty("destination_path")
    private String destinationPath;
    @JsonProperty("recursive")
    private Boolean recursive;
    @JsonProperty("path")
    private String path;
}
