package org.onedatashare.server.model.util;

import lombok.Data;

@Data
public class Times {
  public Long scheduled, started, completed;
}
