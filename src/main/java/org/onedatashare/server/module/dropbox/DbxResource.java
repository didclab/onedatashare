package org.onedatashare.server.module.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.*;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.error.NotFound;
import org.springframework.data.mongodb.core.query.Meta;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DbxResource extends Resource<DbxSession, DbxResource> {

  List<Metadata> nodeMetadata = new LinkedList<Metadata>();

  DbxResource(DbxSession session, String path) {
    super(session, path);
  }

  @Override
  public Mono<DbxResource> select(String name) {
    return session.select(name);
  }

  public Flux<String> list() {
    return initialize().flux().flatMap(resource -> {
      ListFolderResult listing = null;
      try {
        listing = session.client.files().listFolder(path);
      } catch (DbxException e) {
        e.printStackTrace();
      }
      return Flux.fromIterable(listing.getEntries()).map(Metadata::getName);
    });
  }

  public Mono<DbxResource> mkdir() {
    return initialize().doOnSuccess(resource -> {
      try {
        resource.session.client.files().createFolderV2(path);
      } catch (DbxException e) {
        e.printStackTrace();
      }
    });
  }

  public Mono<DbxResource> delete() {
    return initialize().map(resource -> {
      try {
        resource.session.client.files().deleteV2(path);
      } catch (DbxException e) {
        e.printStackTrace();
      }
      return resource;
    });
  }

  public Mono<Stat> stat() {
    return initialize().map(DbxResource::onStat);
  }

  public Stat onStat() {
    Stat stat = new Stat();
    ListFolderResult data = null;
    Metadata mData = null;
    try {
      if (path.equals("/")) {
        data = session.client.files().listFolder("");
      } else {
        try {
          String s = path;
          data = session.client.files().listFolder(path);
        } catch (ListFolderErrorException e) {
          mData = session.client.files().getMetadata(path);
        }
      }
      if (data == null && mData == null)
        throw new NotFound();
      if (data == null) {
        stat = mDataToStat(mData);
      } else {
        if (!data.getEntries().isEmpty()) {
          stat = mDataToStat(data.getEntries().iterator().next());
        }
        stat.dir = true;
        stat.file = false;
      }

      stat.name = path;

      if (stat.dir) {
        ListFolderResult lfr = null;
        if (stat.name.equals("/")) {
          lfr = session.client.files().listFolder("");
        } else {
          // If the metadata is a directory
          if (session.client.files().getMetadata(path) instanceof FolderMetadata) {
            // list the directory files
            lfr = session.client.files().listFolder(path);
          }
          // If the metadata is a file
          else if (session.client.files().getMetadata(path) instanceof FileMetadata) {
            // Return the metadata as a stat object
            stat = mDataToStat(session.client.files().getMetadata(path));
          }
        }
        List<Stat> sub = new LinkedList<>();
        for (Metadata child : lfr.getEntries())
          sub.add(mDataToStat(child));
        stat.setFiles(sub);
      }
    } catch (DbxException e) {
      e.printStackTrace();
    }
    return stat;
  }

  public Mono<Stat> transferStat(){
    return initialize()
            .map(DbxResource::onStat)
            .map(s ->{
              List<Stat> sub = new LinkedList<>();
              long directorySize = 0L;
              try{
                if(s.dir)
                  directorySize =buildDirectoryTree(sub, session.client.files().listFolder(path), "/");
                else{
                  fileResource = true;
                  sub.add(s);
                  directorySize = s.getSize();
                }
              }
              catch (DbxException e) {
                e.printStackTrace();
              }
              s.setFilesList(sub);
              s.setSize(directorySize);
              return s;
            });
  }

  public Long buildDirectoryTree(List<Stat> sub, ListFolderResult lfr, String relativeDirName) throws DbxException{
    long directorySize = 0L;
    for(Metadata childNode : lfr.getEntries()){
      if(childNode instanceof FileMetadata){
        Stat fileStat = mDataToStat(childNode);
        fileStat.setName(relativeDirName + fileStat.getName());
        directorySize += fileStat.size;
        sub.add(fileStat);
      }
      else if(childNode instanceof FolderMetadata){
        directorySize += buildDirectoryTree(sub, session.client.files().listFolder(((FolderMetadata) childNode).getId()),
                                            relativeDirName + childNode.getName()+"/");
      }
    }
    return directorySize;
  }

  private Stat mDataToStat(Metadata data) {
    Stat stat = new Stat(data.getName());
    if (data instanceof FileMetadata) {
      FileMetadata file = (FileMetadata) data;
      stat.file = true;
      stat.size = file.getSize();
      stat.time = file.getClientModified().getTime() / 1000;
    }
    if (data instanceof FolderMetadata) {
      stat.dir = true;
    }
    return stat;
  }

  public DbxTap tap() {
    DbxTap dbxTap = new DbxTap();
    dbxTap.tapStat();
    return dbxTap;
  }

  public DbxDrain sink() {
    return new DbxDrain().start();
//    return slices.doOnNext(dbxDrain::drain).doFinally(s -> dbxDrain.finish());
  }

  public DbxDrain sink(Stat stat){
    return new DbxDrain().start(path + stat.getName());
  }

  public class DbxTap implements Tap {
    DownloadBuilder downloadBuilder;
    long size;
    Stat transferStat;

    public Stat tapStat(){
      transferStat =  transferStat().block();
      return transferStat;
    }

    @Override
    public Stat getTransferStat() {
      return transferStat;
    }

    @Override
    public Flux<Slice> tap(Stat stat, long sliceSize) {
      String downloadPath = "";
      if(!fileResource)
        downloadPath += path;
      downloadBuilder = session.client.files().downloadBuilder(downloadPath +stat.getName());
      size = stat.getSize();
      return tap(sliceSize);
    }

    public Flux<Slice> tap(long sliceSize) {

      return Flux.generate(
              () -> 0L,
              (state, sink) -> {
                //System.out.println("size: "+size);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                if (state + sliceSize < size) {
                  try {
                    downloadBuilder.range(state, sliceSize).start().download(outputStream);
                  } catch (DbxException | IOException e) {
                    e.printStackTrace();
                  }
                  sink.next(new Slice(outputStream.toByteArray()));
                  try {
                    outputStream.close();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                } else {
                  try {
                    downloadBuilder.range(state, size - state).start().download(outputStream);
                  } catch (DbxException | IOException e) {
                    e.printStackTrace();
                  }
                  sink.next(new Slice(outputStream.toByteArray()));
                  sink.complete();
                }
                //System.out.println("size1: " +state + sliceSize);
                return state + sliceSize;
              });
    }
  }

  class DbxDrain implements Drain {
    final long CHUNKED_UPLOAD_CHUNK_SIZE = 1L << 20; // 1MiB
    String drainPath = path;
    long uploaded = 0L;
    InputStream in = new ByteArrayInputStream(new byte[]{});
    String sessionId;
    UploadSessionCursor cursor;

    public DbxDrain start(String drainPath){
      this.drainPath = drainPath;
      return start();
    }

    public DbxDrain start() {
      try {
        sessionId = session.client.files().uploadSessionStart()
                .uploadAndFinish(in, 0L)
                .getSessionId();
        cursor = new UploadSessionCursor(sessionId, uploaded);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return this;
    }

    public void drain(Slice slice) {
      InputStream sliceInputStream = new ByteArrayInputStream(slice.asBytes());
      try {
        session.client.files().uploadSessionAppendV2(cursor)
                .uploadAndFinish(sliceInputStream, slice.length());
      } catch (DbxException | IOException e) {
        e.printStackTrace();
      }
      uploaded += slice.length();
      cursor = new UploadSessionCursor(sessionId, uploaded);
    }

    public void finish() {
      CommitInfo commitInfo = CommitInfo.newBuilder(drainPath)
              .withMode(WriteMode.ADD)
              .withClientModified(new Date())
              .build();
      try {
        FileMetadata metadata = session.client.files().uploadSessionFinish(cursor, commitInfo)
                .uploadAndFinish(in, 0L);
      } catch (DbxException | IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Mono<String> generateDownloadLink(){
    String downloadLink="";
    try {
//      downloadLink = session.client.sharing().createSharedLinkWithSettings(path).getUrl();    // throws an exception if a shared link already exists
      downloadLink = session.client.files().getTemporaryLink(path).getLink();    //temporary link valid for 4 hours

    }
    catch(DbxException dbxe){
      System.out.println("Error encountered while generating shared link for " + path);
      System.out.println(dbxe);
    }
    return Mono.just(downloadLink);
  }
}
