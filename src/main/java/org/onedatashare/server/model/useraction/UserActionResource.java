package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.credential.UploadCredential;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class  UserActionResource {
  public String uri;
  public String id;
  public UserActionCredential credential;
  @Transient
  public UploadCredential uploader;
  @Transient
  public ArrayList<IdMap> map;
  public String type;
}
