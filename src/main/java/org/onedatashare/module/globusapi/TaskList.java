package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TaskList {

    @JsonProperty("DATA_TYPE")
    private String dataType;

    @JsonProperty("length")
    private int length;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("offset")
    private int offset;

    @JsonProperty("total")
    private int total;

    @JsonProperty("DATA")
    private List<Task> data;
}
