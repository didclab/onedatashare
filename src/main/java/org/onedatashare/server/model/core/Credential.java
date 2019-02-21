package org.onedatashare.server.model.core;

import lombok.Data;

@Data
public abstract class Credential {
  public CredentialType type;

  public enum CredentialType {
    OAUTH("oauth"),
    USERINFO("userinfo"),
    GLOBUS("globus");

    private final String text;

    CredentialType(final String text){
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
