package org.onedatashare.server.model.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ODSError {
  public NotFoundException() {
    super("Not Found");
    type = "NotFound";
    error = "Not Found";
    status = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public NotFoundException(String message){
    super(message);
    type = "NotFound";
    error = message;
    status = HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
