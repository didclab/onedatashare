package org.onedatashare.server.model.credential;

import lombok.Data;
import org.onedatashare.server.model.core.Credential;

@Data
public class OAuthCredential extends Credential {
  public transient String token;
  public String name;
  public String dropboxID;
  public String refreshToken;

  public OAuthCredential(String token) {
    this.type = CredentialType.OAUTH;
    this.token = token;
  }
}
