package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.credential.UploadCredential;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionResource {
  public String uri;
  public String id;
  public UserActionCredential credential;
  public UploadCredential uploader;
  public ArrayList<IdMap> map;
  public String type;
}
