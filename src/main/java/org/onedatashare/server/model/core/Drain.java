package org.onedatashare.server.model.core;

public interface Drain {
  Drain start();

  Drain start(String drainPath);    // added for folder transfers

  void drain(Slice slice);

  void finish();

}
