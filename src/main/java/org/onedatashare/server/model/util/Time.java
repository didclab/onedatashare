package org.onedatashare.server.model.util;

public class Time {
  private static long absoluteBase = System.currentTimeMillis();
  private static long relativeBase = System.nanoTime();

  private volatile long start = now(), total = 0;

  /** Get the elapsed time of this {@code Time}. */
  public synchronized long elapsed() {
    return running() ? now()-start + total : total;
  }

  /** Pause the {@code Time}. Has no effect if stopped. */
  public synchronized void stop() {
    if (!running()) return;
    total += now()-start;
    start = -1;
  }

  /** Resume this {@code Time}. Has no effect if running. */
  public synchronized void resume() {
    if (!running()) start = now();
  }

  /** Check if this {@code Time} is running. */
  public synchronized boolean running() { return start > 0; }

  public String toString() { return format(elapsed()); }

  /** Get the current Unix time in milliseconds. */
  public static long now() {
    return absoluteBase + (System.nanoTime() - relativeBase) / (long)1E6;
  }

  /** Get the elapsed time since a given time (in milliseconds). */
  public static long since(long time) {
    return (time < 0) ? 0 : now()-time;
  }

  /** Get the remaining time until a given time (in milliseconds). */
  public static long until(long time) {
    return (time < 0) ? 0 : time-now();
  }

  /** Format a duration (in milliseconds) for human readability. */
  public static String format(long time) {
    if (time < 0) return "-"+format(-time);

    long i = (time)%1000,      // Milliseconds
            s = (time/=1000)%60,  // Seconds
            m = (time/=60)%60,    // Minutes
            h = (time/=60)%24,    // Hours
            d = (time)/24;        // Days

    return (d > 0) ? String.format("%dd%02dh%02dm%02ds", d, h, m, s) :
            (h > 0) ? String.format("%dh%02dm%02ds", h, m, s) :
                    (m > 0) ? String.format("%dm%02ds", m, s) :
                            (s > 0) ? String.format("%d.%02ds", s, i/10) :
                                    String.format("%dms", i);
  }
}
