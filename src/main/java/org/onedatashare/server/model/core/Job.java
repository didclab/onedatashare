package org.onedatashare.server.model.core;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.model.util.Time;
import org.onedatashare.server.model.util.Times;
import org.onedatashare.server.model.util.TransferInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Document
public class Job {
  public JobStatus status = JobStatus.scheduled;

  public UserActionResource src, dest;

  public String message;

  /** Byte progress of the transfer. */
  public TransferInfo bytes;
  /** File progress of the transfer. Currently unused. */
  public TransferInfo files;

  public int attempts = 0, max_attempts = 10;

  /** An ID meaningful to the user who owns the job. */
  public int job_id;

  /** The owner of the job. */
  public String owner;

  @Id
  public UUID uuid;

  public synchronized UUID uuid() {
    if (uuid == null)
      uuid = UUID.randomUUID();
    return uuid;
  }

  /** Times of various important events. */
  public Times times = new Times();

  public Job(UserActionResource src, UserActionResource dest) {
    uuid();
    setSrc(src);
    setDest(dest);
    setBytes(new TransferInfo());
  }

  public Job updateJobWithTransferInfo(TransferInfo info) {
    setBytes(info);
    return this;
  }

  public synchronized Job setStatus(JobStatus status) {
    if (status == null || status.isFilter)
      throw new Error("Cannot set job state to status: "+status);

    if (this.status == status)
      return this;

    // Handle entering the new state.
    switch (this.status = status) {
      case scheduled:
        times.scheduled = Time.now(); break;
      case processing:
        times.started = Time.now(); break;
      case removed:
      case failed:
      case complete:
        times.completed = Time.now(); break;
    } return this;
  }
}
