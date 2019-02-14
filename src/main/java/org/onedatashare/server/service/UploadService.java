package org.onedatashare.server.service;

import org.onedatashare.server.controller.UploadCredential;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class UploadService {

    @Autowired
    UserService userService;

    @Autowired
    JobService jobService;

    @Autowired
    ResourceServiceImpl resourceService;

    private static Map<UUID, Queue<Slice>> ongoingUploads = new HashMap<UUID, Queue<Slice>>();

    public Mono<Integer> uploadChunk(String cookie, UUID uuid,
                                 Mono<FilePart> filePart, String credential,
                                 String directoryPath, String fileName, Long totalFileSize) {
        if (ongoingUploads.containsKey(uuid)) {
            return sendFilePart(filePart, ongoingUploads.get(uuid));
        } else {
            UserAction ua = new UserAction();
            ua.src = new UserActionResource();
            ua.src.uri = "Upload";
            Queue<Slice> uploadQueue = new LinkedBlockingQueue<Slice>();
            ua.src.uploader = new UploadCredential(uploadQueue, totalFileSize, fileName);

            ua.dest = new UserActionResource();
            if(directoryPath.endsWith("/")){
                ua.dest.uri = directoryPath+fileName;
            }else{
                ua.dest.uri = directoryPath+"/"+fileName;
            }

            UserActionCredential uao = new UserActionCredential();
            uao.uuid = credential;
            ua.dest.credential = uao;
            resourceService.submit(cookie, ua).subscribe();
            return sendFilePart(filePart, uploadQueue).map(size -> {
                if (size < totalFileSize) {
                    ongoingUploads.put(uuid, uploadQueue);
                }
                return size;
            });
        }
    }

    public Mono<Integer> sendFilePart(Mono<FilePart> pfr, Queue<Slice> qugue){

        //ongoingUploads.get(uuid).onNext(pfr);

        return pfr.flatMapMany(fp -> fp.content())
        .map(content ->  {
                Slice slc = new Slice(content.asByteBuffer());
                qugue.add(slc);
                return slc.length();
            }
        ).reduce(0, (x1, x2) -> x1 + x2);
    }

    public Mono<Void> finishUpload(UUID uuid) {
        if(!ongoingUploads.containsKey(uuid)){
            return Mono.error(null);
        }
        ongoingUploads.remove(uuid);
        return Mono.just(null);
    }
}
