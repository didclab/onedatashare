package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.net.URI;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MkdirRequest {
    @JsonProperty("path")
    private String path;
    @JsonProperty("DATA_TYPE")
    private String dataType;
}