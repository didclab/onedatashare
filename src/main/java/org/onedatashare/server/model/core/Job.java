package org.onedatashare.server.model.core;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.model.util.Time;
import org.onedatashare.server.model.util.Times;
import org.onedatashare.server.model.util.TransferInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * Model that holds transfer job information.
 * Represents the document that is eventually stored in the MongoDB Job collection.
 */
@Document
@Data
public class Job {
  private JobStatus status = JobStatus.scheduled;

  private UserActionResource src, dest;

  private String message;

  /** Byte progress of the transfer. */
  private TransferInfo bytes;
  /** File progress of the transfer. Currently unused. */
  private TransferInfo files;

  private int attempts = 0, max_attempts = 10;

  /** An ID meaningful to the user who owns the job. */
  private int job_id;

  /** To mark job as deleted **/
  private boolean deleted = false;

  /** The owner of the job. */
  private String owner;

  /** Identifiers for jobs restarted using restart job functionality */
  public Boolean restartedJob = false;
  public Integer sourceJob = null;

  @Id
  private UUID uuid;

  public synchronized UUID uuid() {
    if (uuid == null)
      uuid = UUID.randomUUID();
    return uuid;
  }

  /** Times of various important events. */
  private Times times = new Times();

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

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Job)) return false;
    final Job other = (Job) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$status = this.status;
    final Object other$status = other.status;
    if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
    final Object this$src = this.src;
    final Object other$src = other.src;
    if (this$src == null ? other$src != null : !this$src.equals(other$src)) return false;
    final Object this$dest = this.dest;
    final Object other$dest = other.dest;
    if (this$dest == null ? other$dest != null : !this$dest.equals(other$dest)) return false;
    final Object this$message = this.message;
    final Object other$message = other.message;
    if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
    final Object this$bytes = this.bytes;
    final Object other$bytes = other.bytes;
    if (this$bytes == null ? other$bytes != null : !this$bytes.equals(other$bytes)) return false;
    final Object this$files = this.files;
    final Object other$files = other.files;
    if (this$files == null ? other$files != null : !this$files.equals(other$files)) return false;
    if (this.attempts != other.attempts) return false;
    if (this.max_attempts != other.max_attempts) return false;
    if (this.job_id != other.job_id) return false;
    final Object this$owner = this.owner;
    final Object other$owner = other.owner;
    if (this$owner == null ? other$owner != null : !this$owner.equals(other$owner)) return false;
    final Object this$uuid = this.uuid;
    final Object other$uuid = other.uuid;
    if (this$uuid == null ? other$uuid != null : !this$uuid.equals(other$uuid)) return false;
    final Object this$times = this.times;
    final Object other$times = other.times;
    if (this$times == null ? other$times != null : !this$times.equals(other$times)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Job;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $status = this.status;
    result = result * PRIME + ($status == null ? 43 : $status.hashCode());
    final Object $src = this.src;
    result = result * PRIME + ($src == null ? 43 : $src.hashCode());
    final Object $dest = this.dest;
    result = result * PRIME + ($dest == null ? 43 : $dest.hashCode());
    final Object $message = this.message;
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    final Object $bytes = this.bytes;
    result = result * PRIME + ($bytes == null ? 43 : $bytes.hashCode());
    final Object $files = this.files;
    result = result * PRIME + ($files == null ? 43 : $files.hashCode());
    result = result * PRIME + this.attempts;
    result = result * PRIME + this.max_attempts;
    result = result * PRIME + this.job_id;
    final Object $owner = this.owner;
    result = result * PRIME + ($owner == null ? 43 : $owner.hashCode());
    final Object $uuid = this.uuid;
    result = result * PRIME + ($uuid == null ? 43 : $uuid.hashCode());
    final Object $times = this.times;
    result = result * PRIME + ($times == null ? 43 : $times.hashCode());
    return result;
  }

  public String toString() {
    return "Job(status=" + this.status + ", src=" + this.src + ", dest=" + this.dest + ", message=" + this.message + ", bytes=" + this.bytes + ", files=" + this.files + ", attempts=" + this.attempts + ", max_attempts=" + this.max_attempts + ", job_id=" + this.job_id + ", owner=" + this.owner + ", uuid=" + this.uuid + ", times=" + this.times + ")";
  }
}
