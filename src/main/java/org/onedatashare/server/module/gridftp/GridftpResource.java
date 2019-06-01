package org.onedatashare.server.module.gridftp;

import com.dropbox.core.v2.files.*;
import org.onedatashare.module.globusapi.*;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.service.GridftpService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Resource that provides services specific to Grid FTP endpoint.
 */
public class GridftpResource extends Resource<GridftpSession, GridftpResource> {
    private Boolean showHidden = true;
    private Integer limit = Integer.MAX_VALUE;
    private Integer offset = 0;
    private String orderedBy = "Name";
    private String filter = null;

    GridftpResource(GridftpSession session, String path) {
        super(session, path);
    }
    @Override
    public Mono<GridftpResource> select(String name) {
        return getSession().select(name);
    }

    public Mono<GridftpResource> mkdir() {
        return initialize().flatMap(
                resource -> resource.getSession().client.mkdir(getSession().endpoint.getId(), getPath()))
                .map(u -> this);
    }

    public Mono<Result> transferTo(GridftpResource grsf){
        return getSession().client.getJobSubmissionId()
        .flatMap(response -> {
            TaskSubmissionRequest request = new TaskSubmissionRequest();
            request.setDataType("transfer");
            request.setSubmissionId(response.getValue());
            request.setSourceEndpoint(getSession().endpoint.getId());
            request.setDestinationEndpoint(grsf.getSession().endpoint.getId());
            List<TaskItem> data = new ArrayList<>();
            TaskItem item = new TaskItem();
            item.setDataType("transfer_item");
            item.setRecursive(true);
            item.setSourcePath(GridftpService.pathFromUri(this.getPath()));
            item.setDestinationPath(GridftpService.pathFromUri(grsf.getPath()));
            data.add(item);
            request.setData(data);
            return getSession().client.submitTask(request);
        });
    }

    public Mono<Result> deleteV2() {
        return initialize()
            .flatMap(resource -> getSession().client.getJobSubmissionId())
            .flatMap(result -> {
                TaskSubmissionRequest tr = new TaskSubmissionRequest();
                tr.setSubmissionId(result.getValue());
                tr.setEndpoint(getSession().endpoint.getId());
                tr.setDataType("delete");
                tr.setLabel("delete kabrl");
                tr.setRecursive(true);
                tr.setIgnoreMissing(true);
                List<TaskItem> tsl = new ArrayList<TaskItem>();
                TaskItem ti = new TaskItem();
                ti.setPath(this.getPath());
                ti.setDataType("delete_item");
                tsl.add(ti);
                tr.setData(tsl);
                return getSession().client.submitTask(tr);
            });
    }

    public Mono<Stat> stat() {
        return initialize().flatMap(GridftpResource::onStat);
    }

    public Mono<Stat> onStat() {
        return getSession().client
                .listFiles(getSession().endpoint.getId(), getPath(), showHidden, offset, limit, orderedBy, filter)
                .map(
                    fileList -> {
                    Stat stat = new Stat();
                    Stat filesStat[] = new Stat[fileList.getData().size()];
                    for(int i = 0; i < fileList.getData().size(); i++){
                        Stat tempStat = new Stat();
                        File file = fileList.getData().get(i);
                        tempStat.setFile(file.getType().equals("file"));
                        tempStat.setSize(file.getSize());
                        tempStat.setName(file.getName());
                        tempStat.setDir(file.getType().equals("dir"));
                        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
                        try {
                            tempStat.setTime(fromUser.parse(file.getLastModified()).getTime()/1000);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        tempStat.setPermissions(file.getPermissions());
                        tempStat.setLink(file.getLinkTarget());
                        filesStat[i] = tempStat;
                    }
                    stat.setFiles(filesStat);
                    return stat;
                }
        );
    }

    private Stat mDataToStat(Metadata data) {
        Stat stat = new Stat(data.getName());
        if (data instanceof FileMetadata) {
            FileMetadata file = (FileMetadata) data;
            stat.setFile(true);
            stat.setSize(file.getSize());
            stat.setTime(file.getClientModified().getTime() / 1000);
        }
        if (data instanceof FolderMetadata) {
            stat.setDir(true);
        }
        return stat;
    }

    public GridftpResource.GridftpTap tap() {
        return new GridftpResource.GridftpTap();
    }

    public GridftpResource.GridftpDrain sink() {
        return new GridftpResource.GridftpDrain().start();
//    return slices.doOnNext(dbxDrain::drain).doFinally(s -> dbxDrain.finish());
    }

    @Override
    public Mono<Stat> getTransferStat() {
        return null;
    }

    class GridftpTap implements Tap {
        final long size = stat().block().getSize();

        public Flux<Slice> tap(long sliceSize) {
            return Flux.empty();
        }

        @Override
        public Flux<Slice> tap(Stat stat, long sliceSize) {
            return null;
        }
    }

    class GridftpDrain implements Drain {
        final long CHUNKED_UPLOAD_CHUNK_SIZE = 1L << 20; // 1MiB
        long uploaded = 0L;
        InputStream in = new ByteArrayInputStream(new byte[]{});
        String sessionId;
        UploadSessionCursor cursor;

        public GridftpResource.GridftpDrain start() {
              return this;
        }

    @Override
    public Drain start(String drainPath) {
        return null;
    }


        public void drain(Slice slice) {
        }

        public void finish() {
        }
    }
}

