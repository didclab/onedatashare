package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationRequirement {

    public ActivationRequirement(String name,  String type, String uiName, String value, boolean isPrivate){
        this.name = name;
        this.type = type;
        this.uiName = uiName;
        this.value = value;
        this.isPrivate = isPrivate;
    }

    @JsonProperty("DATA_TYPE")
    private String dataType = "activation_requirement";

    @JsonProperty("description")
    private String description;

    @JsonProperty("name")
    private String name;

    @JsonProperty("private")
    private boolean isPrivate;

    @JsonProperty("required")
    private boolean isRequired = true;

    @JsonProperty("type")
    private String type;

    @JsonProperty("ui_name")
    private String uiName;

    @JsonProperty("value")
    private String value;

}
