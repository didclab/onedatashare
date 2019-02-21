package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class NotFound extends ODSError {
  public NotFound() {
    super("Not Found");
    type = "NotFound";
    error = "Not Found";
    status = HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
