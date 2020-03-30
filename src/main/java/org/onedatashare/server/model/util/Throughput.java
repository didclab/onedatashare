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

import static org.onedatashare.server.model.util.Time.now;

/**
 * A class for keeping track of instantaneous throughput. Also provides some
 * utility methods for formatting throughput.
 */
public class Throughput {
  private long t = now();  // Time of last sample.
  private double q;        // Time quantum (in ms).
  private double th = 0;   // Running throughput estimate.

  /** Create a {@code Throughput} with the default quantum (3000ms). */
  public Throughput() { this(1E3); }

  /** Create a {@code Throughput} with the given quantum. */
  public Throughput(double quantum) {
    q = (quantum <= 0) ? 0 : quantum;
  }

  /** Create a {@code Throughput} from {@code bytes} and {@code time}. */
  public Throughput(double bytes, double time) {
    q = 0;
    th = bytes/time;
    if(time == 0.0){
      /* For very small file size, the timer ticks only 0.0.
       So a very small value ( 0.0000001) is taken instead of 0.0*/
      th = bytes/0.0000001;
    }
  }

  /** Update the throughput estimation with the given amount. */
  public synchronized void update(double amount) {
    if (q <= 0) return;
    long now = now();
    double d = now-t;
    t = now;
    th = (d > q) ? amount/q : th*(1-d/q) + amount/q;
    if (Double.isInfinite(th) || Double.isNaN(th))
      th = 0;
  }

  /** Get the throughput in units per second. */
  public synchronized double value() {
    if (q <= 0) return th;
    double d = now()-t;
    double v = (d > q) ? 0 : th*(1-d/q)*1000;
    return (Double.isInfinite(v) || Double.isNaN(v)) ? 0 : v;
  }

  /** Format {@code throughput} in a human-readable way. */
  public static String format(double throughput) {
    return format(throughput, ' ');
  } private static String format(double tp, char pre) {
    return prettySize(tp)+"B/s";
  }

  public String toString() { return format(value()); }

  public static String prettySize(long s) {
    return prettySize((double)s, (char)0);
  } public static String prettySize(double s) {
    return prettySize(s, (char)0);
  } private static String prettySize(double s, char pre) {
    if (s < 0) {
      return "-"+prettySize(-s, pre);
    } if (s >= 1000) switch (pre) {
      // Uppercase characters for base 10.
      case  0 : return prettySize(s/1000, 'k');
      case 'k': return prettySize(s/1000, 'M');
      case 'M': return prettySize(s/1000, 'G');
      case 'G': return prettySize(s/1000, 'T');
    } if (pre == 0) {
      return String.format("%d", (int) s);
    } return String.format("%.02f%c", s, pre);
  }
}

