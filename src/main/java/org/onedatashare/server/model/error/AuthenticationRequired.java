package org.onedatashare.server.model.error;

import org.onedatashare.server.controller.OauthController;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

public class AuthenticationRequired extends ODSError {
  /** Acceptable credential types. */
  public List<String> options;
  public OAuthCredential cred;
  /**
   * Authentication is required. Optionally, a list of allowed credential types
   * can be provided. These will be reported back to the client so it can
   * display authentication options.
   */
  public AuthenticationRequired(String... options) {
    super("Authentication is required.");
    type = "AuthenticationRequired";
    error = "Authentication is required.";
    status = HttpStatus.INTERNAL_SERVER_ERROR;

    if (options != null && options.length > 0)
      this.options = Arrays.asList(options);
  }
  public AuthenticationRequired(int err,  OAuthCredential cred, String... authStatus) {
    super("Authentication is required.");
    type = "AuthenticationRequired";
    error = "Authentication is required.";
    if(err == 401)
      status = HttpStatus.UNAUTHORIZED;
    else
      status = HttpStatus.BAD_REQUEST;

    this.cred = cred;

    if (authStatus != null && authStatus.length > 0){
        this.options = Arrays.asList(authStatus);

    }

  }
}