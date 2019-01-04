package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAction {
  public String action;
  public String email;
  public String password;
  public String uri;
  public String uuid;
  public int depth;
  public UserActionResource src;
  public UserActionResource dest;
  public UserActionCredential credential;
  public Integer job_id;
}
