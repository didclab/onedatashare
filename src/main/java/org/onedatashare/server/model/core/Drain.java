package org.onedatashare.server.model.core;

public interface Drain {
  Drain start();

  void drain(Slice slice);

  void finish();
}
