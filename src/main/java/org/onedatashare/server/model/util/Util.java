package org.onedatashare.server.model.util;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {

  // Convert a byte array into a formatted string.
  public static String formatBytes(byte[] bytes, String fmt) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append(String.format(fmt, b));
    return sb.toString();
  }
  public static String up(String path){
    Path nioPath = Paths.get(URI.create(path));
    return nioPath.getParent().toString();
  }
}
