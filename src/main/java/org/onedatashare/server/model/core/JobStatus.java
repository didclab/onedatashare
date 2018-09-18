package org.onedatashare.server.model.core;

import java.util.EnumSet;

/** States a job can be in, and some special status filters. */
public enum JobStatus {
  /** The job is scheduled to run in the future. */
  scheduled,
  /** The job is currently in progress. */
  processing,
  /** The job has been paused by request. */
  paused,
  /** The job has been removed by request. */
  removed,
  /** The job failed for some reason. */
  failed,
  /** The job has completed successfully. */
  complete,

  /** Filter for all job statuses. */
  all(false),
  /** Filter for jobs that have not finished. */
  pending(false),
  /** Filter for all terminated jobs regardless of success. */
  done(false);

  /** Indicates whether this is a pseudo-status used for filtering. */
  public final boolean isFilter;
  private EnumSet<JobStatus> filter;

  JobStatus() {
    this(true);
  } JobStatus(boolean real) {
    isFilter = !real;
  }

  /** Get an {@code EnumSet} filter by status name. */
  public static EnumSet<JobStatus> filter(String s) {
    return byName(s).filter();
  }

  /** Get an {@code EnumSet} filter for this status. */
  public EnumSet<JobStatus> filter() {
    if (filter != null) {
      return filter;
    } if (!isFilter) {
      filter = EnumSet.of(this);
    } switch (this) {
      case all:
        return filter = EnumSet.allOf(JobStatus.class);
      case pending:
        return filter = EnumSet.of(scheduled, processing, paused);
      case done:
        return filter = EnumSet.of(removed, failed, complete);
    } return filter;
  }

  /** Get a status by name. */
  public static JobStatus byName(String name) {
    try {
      return Enum.valueOf(JobStatus.class, name.toLowerCase());
    } catch (Exception e) {
      // I guess that's not a real status...
      throw new RuntimeException("Invalid status: "+name);
    }
  }
}

