package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAction {
  public String action;
  public String email;
  public String password;
  public String uri;
  public String id;
  public ArrayList<IdMap> map;
  public String type;
  public String uuid;
  public String code;
  public String confirmPassword;
  public String newPassword;
  public String type;
  public ArrayList<IdMap> map;
  public int depth;
  public UserActionResource src;
  public UserActionResource dest;
  public UserActionCredential credential;
  public Integer job_id;

}
