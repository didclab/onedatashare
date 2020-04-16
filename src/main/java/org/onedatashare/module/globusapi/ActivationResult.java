package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivationResult extends Result{

        private String endpoint;

        @JsonProperty("expire_time")
        private String expireTime;

        @JsonProperty("expires_in")
        private int expiresIn;

}
