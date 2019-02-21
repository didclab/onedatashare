package org.onedatashare.server.model.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.module.globusapi.Result;
import org.onedatashare.server.model.util.Progress;
import org.onedatashare.server.model.util.Throughput;
import org.onedatashare.server.model.util.Time;
import org.onedatashare.server.model.util.TransferInfo;
import org.onedatashare.server.module.gridftp.GridftpResource;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@NoArgsConstructor
@Data
public class Transfer<S extends Resource, D extends Resource> {
  public S source;
  public D destination;

  /** Periodically updated information about the ongoing transfer. */
  public final TransferInfo info = new TransferInfo();

  private Time timer;
  private Progress progress = new Progress();
  private Throughput throughput = new Throughput();

  public Transfer(S source, D destination) {
    this.source = source;
    this.destination = destination;
  }

  public Flux<TransferInfo> start(Long sliceSize) {

    if (source instanceof GridftpResource && destination instanceof GridftpResource){
        ((GridftpResource) source).transferTo(((GridftpResource) destination)).subscribe();
        return Flux.empty();
    }else if (source instanceof GridftpResource || destination instanceof GridftpResource){
        return Flux.error(new Exception("Can not send from GridFTP to other protocols"));
    }

    sliceSize = (sliceSize == null) ? 1024L : sliceSize;

    initialize();
    Tap tap = source.tap();
    Drain drain = destination.sink();

    return tap.tap(sliceSize)
            .subscribeOn(Schedulers.elastic())
            .doOnNext(drain::drain)
            .subscribeOn(Schedulers.elastic())
            .doOnSubscribe(s -> startTimer())
            .map(this::addProgress)
            .doOnComplete(drain::finish)
            .doFinally(s -> done());
  }

  public void initialize() {
    Stat stat = (Stat) source.stat().block();
    info.setTotal(stat.size);
  }

  public void done() {
    timer.stop();
  }

  public void startTimer() {
    timer = new Time();
  }

  public TransferInfo addProgress(Slice slice) {
    long size = slice.length();
    progress.add(size);
    throughput.update(size);
    info.update(timer, progress, throughput);
    return info;
  }

  public Transfer<S, D> setSource(S source) {
    this.source = source;
    return this;
  }

  public Transfer<S, D> setDestination(D destination) {
    this.destination = destination;
    return this;
  }
}
