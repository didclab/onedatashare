package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileList {
    @JsonProperty("DATA_TYPE")
    String dataType;
    @JsonProperty("path")
    String path;
    @JsonProperty("absolute_path")
    String absolutePath;
    @JsonProperty("endpoint")
    String endpoint;
    @JsonProperty("rename_supported")
    Boolean renameSupported;
    @JsonProperty("symlink_supported")
    Boolean symlinkSupported;
    @JsonProperty("DATA")
    List<File> data;
    @JsonProperty("length")
    int length;
}
