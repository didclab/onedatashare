package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndPointList {

    @JsonProperty("DATA_TYPE")
    private String dataType;

    @JsonProperty("has_next_page")
    private boolean hasNextPage;

    private Integer limit;

    private Integer offset;

    @JsonProperty("DATA")
    private List<EndPoint> data;

}
