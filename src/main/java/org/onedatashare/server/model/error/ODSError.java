package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public abstract class ODSError extends RuntimeException {
  public HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
  public String error;
  public String type;

  public ODSError(String reason) {
    super(reason);
    if (reason == null)
      throw new NullPointerException();
  }
}