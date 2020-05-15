package org.onedatashare.module.globusapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndPoint{

    private String id;

    private String name;

    @JsonProperty("canonical_name")
    private String canonicalName;

    @JsonProperty("expire_time")
    private String expiryTime;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("myproxy_dn")
    private String myProxyDomainName;

    @JsonProperty("myproxy_server")
    private String myProxyServer;

    private String activated;

    @JsonProperty("DATA")
    private List<Server> data;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Server{

        @JsonProperty("hostname")
        private String hostName;

        @JsonProperty("uri")
        private String uri;

        private String port;

        private String scheme;

    }
}
