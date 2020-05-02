package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationRequest {

    @JsonProperty("DATA")
    private List<ActivationRequirement> data;

    @JsonProperty("DATA_TYPE")
    private String dataType = "activation_requirements";

    @JsonProperty("activated")
    private boolean isActivated;

    @JsonProperty("auto_activation_supported")
    private boolean isAutoActivationSupported;

    @JsonProperty("expires_in")
    private int expiresIn;

}
