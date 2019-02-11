package org.onedatashare.server.service;

import com.google.common.eventbus.Subscribe;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.controller.UploadCredential;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.model.util.TransferInfo;
import org.onedatashare.server.module.clientupload.ClientUploadResource;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.*;

@Service
public class UploadService {

    @Autowired
    UserService userService;

    @Autowired
    JobService jobService;

    @Autowired
    ResourceServiceImpl resourceService;

    private static Map<UUID, FluxSink<Slice>> ongoingUploads = new HashMap<UUID, FluxSink<Slice>>();

    public Mono<Job> uploadChunk(String cookie, UUID uuid,
                                 Mono<FilePart> filePart, String credential,
                                 String directoryPath, String fileName, Long totalFileSize) {
        if (ongoingUploads.containsKey(uuid)) {
            sendFilePart(filePart, uuid);
            return Mono.just(new Job(null, null));
        } else {
            UserAction ua = new UserAction();
            ua.src = new UserActionResource();
            ua.src.uri = "Upload";
            FluxProcessor processor = DirectProcessor.create().serialize();
            ongoingUploads.put(uuid, processor.sink());
            ua.src.uploader = new UploadCredential(processor.publish(), totalFileSize, fileName);
            //ua.src.uploader.getFux().subscribe();

            ua.dest = new UserActionResource();
            if(directoryPath.endsWith("/")){
                ua.dest.uri = directoryPath+fileName;
            }else{
                ua.dest.uri = directoryPath+"/"+fileName;
            }
            UserActionCredential uao = new UserActionCredential();
            uao.uuid = credential;
            sendFilePart(filePart, uuid);
            return resourceService.submit(cookie, ua);
        }
    }

    public void sendFilePart(Mono<FilePart> pfr, UUID uuid){
        pfr.flatMapMany(fp -> fp.content())
        .map(content -> {
            ongoingUploads.get(uuid)
                .next(new Slice(content.asByteBuffer()));
            return null;
        }).subscribe();
    }

    public Mono<Void> finishUpload(UUID uuid) {
        if(!ongoingUploads.containsKey(uuid)){
            return Mono.error(null);
        }
        ongoingUploads.remove(uuid);
        return Mono.just(null);
    }
}
