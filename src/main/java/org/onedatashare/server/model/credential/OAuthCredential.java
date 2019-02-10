package org.onedatashare.server.model.credential;

import org.onedatashare.server.model.core.Credential;
import java.util.Date;
public class OAuthCredential extends Credential {
  public transient String token;
  public String name;
  public String dropboxID;
  public Date expiredTime;

  public OAuthCredential(String token) {
    this.type = CredentialType.OAUTH;
    this.token = token;
  }
}
