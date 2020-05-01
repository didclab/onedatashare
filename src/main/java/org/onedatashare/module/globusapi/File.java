package org.onedatashare.module.globusapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class File {
    @JsonProperty("DATA_TYPE")
    String dataType;
    @JsonProperty("name")
    String name;
    @JsonProperty("type")
    String type;
    @JsonProperty("link_target")
    String linkTarget;
    @JsonProperty("symlink_supported")
    Boolean symlinkSupported;
    @JsonProperty("user")
    String user;
    @JsonProperty("link_user")
    String linkUser;
    @JsonProperty("group")
    String group;
    @JsonProperty("link_group")
    String linkGroup;
    @JsonProperty("permissions")
    String permissions;
    @JsonProperty("last_modified")
    String lastModified;
    @JsonProperty("link_last_modified")
    String linkLastModified;
    @JsonProperty("size")
    Long size;
    @JsonProperty("link_size")
    Long linkSize;
}
