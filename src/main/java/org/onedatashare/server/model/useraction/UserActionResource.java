package org.onedatashare.server.model.useraction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.credential.UploadCredential;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionResource {
  public String uri;
  public UserActionCredential credential;
  public UploadCredential uploader;
}