package org.onedatashare.server.model.util;

/**
 * A utility for measuring the progress of some operation.
 */
public class Progress {
  private long done = 0, total = 0;

  public Progress() { }

  public Progress(long total) {
    this.total = total;
  }

  public synchronized long done() { return done; }

  public synchronized long total() { return total; }

  public synchronized long remaining() { return total-done; }

  /**
   * Finish this progress automatically. That is, set {@code done} to be equal
   * to {@code total}. Used for example when we know a transfer has completed
   * successfully and we don't want to fill the progress the rest of the way.
   */
  public synchronized void finish() {
    done = total;
  }

  public synchronized void add(long done, long total) {
    this.done += done;
    this.total += total;
  }

  public synchronized void add(long done) {
    this.done += done;
  }

  /** Get the progress rate based on time. */
  public synchronized Throughput rate(Time time) {
      // time is the value of timer from Transfer class
    return new Throughput(done(), time.elapsed()/1000);
  }

  /** Get the progress as a percentage value. */
  public synchronized double toPercent() {
    return (total <= 0) ? 0 : 100.0 * done / total;
  }

  /** Get the progress as a percentage {@code String}. */
  public synchronized String toPercentString() {
    return String.format("%.0f%%", toPercent());
  }

  public synchronized String toString() {
    return (total <= 0) ?
            Throughput.prettySize(done) :
            Throughput.prettySize(done)+"/"+Throughput.prettySize(total);
  }
}
