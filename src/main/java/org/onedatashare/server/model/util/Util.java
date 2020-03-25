/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


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
