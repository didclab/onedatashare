package org.onedatashare.server.model.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.server.model.util.Progress;
import org.onedatashare.server.model.util.Throughput;
import org.onedatashare.server.model.util.Time;
import org.onedatashare.server.model.util.TransferInfo;
import org.onedatashare.server.module.gridftp.GridftpResource;
import org.onedatashare.server.module.http.HttpResource;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@NoArgsConstructor
@Data
public class Transfer<S extends Resource, D extends Resource> {
  public S source;
  public D destination;

  /** Periodically updated information about the ongoing transfer. */
  public final TransferInfo info = new TransferInfo();

  // Timer counts 0.0 for files with very small size
  protected Time timer;
  protected Progress progress = new Progress();
  protected Throughput throughput = new Throughput();

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
    // HTTP is read only
    if(destination instanceof HttpResource)
      return Flux.error(new Exception("HTTP is read-only"));
    System.out.println("Hi");

    Stat tapStat = (Stat)source.getTransferStat().block();
    info.setTotal(tapStat.size);

    return Flux.fromIterable(tapStat.getFilesList())
            .doOnSubscribe(s -> startTimer())
            .flatMap(fileStat -> {
              final Drain drain;
              if(tapStat.isDir())
                drain = destination.sink(fileStat);
              else
                drain = destination.sink();
              return source.tap().tap(fileStat, sliceSize)
                      .subscribeOn(Schedulers.elastic())
                      .doOnNext(drain::drain)
                      .subscribeOn(Schedulers.elastic())
                      .map(this::addProgress)
                      .doOnComplete(drain::finish);
            }).doFinally(s -> done());
  }

  public void initialize() {
    Stat stat = (Stat) source.stat().block();
    info.setTotal(stat.size);
  }

  public void initializeUpload(int fileSize){
    info.setTotal(fileSize);
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
      System.out.println("Source set to " + source.getClass());
    this.source = source;
    return this;
  }

  public Transfer<S, D> setDestination(D destination) {
      System.out.println("Destination set to " + destination.getClass());
    this.destination = destination;
    return this;
  }


  /**
   * This method was developed for debugging purposes.
   * This method ensures that the transfer is performed sequentially.
   * @param sliceSize
   * @return TransferInfo - returned purposely to satisfy return constraint
   */
  public Flux<TransferInfo> blockingStart(Long sliceSize) {

    if (source instanceof GridftpResource && destination instanceof GridftpResource){
      ((GridftpResource) source).transferTo(((GridftpResource) destination)).subscribe();
      return Flux.empty();
    }else if (source instanceof GridftpResource || destination instanceof GridftpResource){
      return Flux.error(new Exception("Can not send from GridFTP to other protocols"));
    }

    Stat tapStat = (Stat)source.getTransferStat().block();
    info.setTotal(tapStat.size);

    startTimer();
    for(Stat fileStat : tapStat.getFilesList()){
      final Drain drain;
      if(tapStat.isDir())
        drain = destination.sink(fileStat);
      else
        drain = destination.sink();
      source.tap().tap(fileStat, sliceSize)
              .subscribeOn(Schedulers.elastic())
              .doOnNext(drain::drain)
              .subscribeOn(Schedulers.elastic())
              .map(this::addProgress)
              .blockLast();
      drain.finish();
      System.out.println(fileStat.getName() + " transferred");
    }
    done();
    return Flux.just(info);

  }
}
