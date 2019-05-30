package org.onedatashare.server.module.gridftp;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.*;
import com.google.api.client.util.DateTime;
import org.apache.commons.net.ntp.TimeStamp;
import org.onedatashare.module.globusapi.*;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.util.TransferInfo;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;
import org.onedatashare.server.service.GridftpService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
//import sun.rmi.transport.Endpoint;

//import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        return session.select(name);
    }


    public Mono<GridftpResource> mkdir() {
        return initialize().flatMap(
                resource -> resource.session.client.mkdir( session.endpoint.getId(), this.path))
                .map(u -> this);
    }

    public Mono<Result> transferTo(GridftpResource grsf){
        return session.client.getJobSubmissionId()
        .flatMap(response -> {
            TaskSubmissionRequest request = new TaskSubmissionRequest();
            request.setDataType("transfer");
            request.setSubmissionId(response.getValue());
            request.setSourceEndpoint(session.endpoint.getId());
            request.setDestinationEndpoint(grsf.session.endpoint.getId());
            List<TaskItem> data = new ArrayList<>();
            TaskItem item = new TaskItem();
            item.setDataType("transfer_item");
            item.setRecursive(true);
            item.setSourcePath(GridftpService.pathFromUri(this.getPath()));
            item.setDestinationPath(GridftpService.pathFromUri(grsf.getPath()));
            data.add(item);
            request.setData(data);
            return session.client.submitTask(request);
        });
    }

    public Mono<Result> deleteV2() {
        return initialize()
            .flatMap(resource -> session.client.getJobSubmissionId())
            .flatMap(result -> {
                TaskSubmissionRequest tr = new TaskSubmissionRequest();
                tr.setSubmissionId(result.getValue());
                tr.setEndpoint(session.endpoint.getId());
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
                return session.client.submitTask(tr);
            });
    }

    public Mono<Stat> stat() {
        return initialize().flatMap(GridftpResource::onStat);
    }

    public Mono<Stat> onStat() {
        return session.client
                .listFiles(session.endpoint.getId(), this.path, showHidden, offset, limit, orderedBy, filter)
                .map(
                    fileList -> {
                    Stat st = new Stat();
                    Stat files[] = new Stat[fileList.getData().size()];
                    for(int i = 0; i < fileList.getData().size(); i++){
                        Stat temps = new Stat();
                        File file = fileList.getData().get(i);
                        temps.file = file.getType().equals("file");
                        temps.size = file.getSize();
                        temps.name = file.getName();
                        temps.dir = file.getType().equals("dir");
                        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
                        try {
                            temps.time = fromUser.parse(file.getLastModified()).getTime()/1000;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        temps.perm = file.getPermissions();
                        temps.link = file.getLinkTarget();
                        files[i] = temps;
                    }
                    st.setFiles(files);
                    return st;
                }
        );
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
        final long size = stat().block().size;

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

