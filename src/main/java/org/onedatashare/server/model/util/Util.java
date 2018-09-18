package org.onedatashare.server.model.util;

public class Util {

  // Convert a byte array into a formatted string.
  public static String formatBytes(byte[] bytes, String fmt) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append(String.format(fmt, b));
    return sb.toString();
  }
}
