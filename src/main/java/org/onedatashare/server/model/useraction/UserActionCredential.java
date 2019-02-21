package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.module.globusapi.EndPoint;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionCredential {
  public String type;
  public String uuid;
  public String username;
  public String password;
  public EndPoint globusEndpoint;
}
