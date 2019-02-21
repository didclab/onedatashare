package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionResource {
  public String uri;
  public String id;
  public UserActionCredential credential;
  public ArrayList<IdMap> map;
  public String type;
}