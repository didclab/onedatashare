package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.module.globusapi.EndPoint;
import org.springframework.data.annotation.Transient;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionCredential {
  public String type;
  public String uuid;
  @Transient
  public String username;
  @Transient
  public String password;
  public EndPoint globusEndpoint;
}
