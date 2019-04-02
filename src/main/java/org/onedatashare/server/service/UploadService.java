package org.onedatashare.server.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.credential.UploadCredential;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
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

    private static Map<UUID, LinkedBlockingQueue<Slice>> ongoingUploads = new HashMap<UUID, LinkedBlockingQueue<Slice>>();

    public Mono<Integer> uploadChunk(String cookie, UUID uuid, Mono<FilePart> filePart, String credential,
                                     String directoryPath, String fileName, Long totalFileSize, String googledriveid, String idmap) {
        if (ongoingUploads.containsKey(uuid)) {
            return sendFilePart(filePart, ongoingUploads.get(uuid));
        } else {
            UserAction userAction = new UserAction();
            userAction.src = new UserActionResource();
            userAction.src.uri = "Upload";
            LinkedBlockingQueue<Slice> uploadQueue = new LinkedBlockingQueue<Slice>();
            userAction.src.uploader = new UploadCredential(uploadQueue, totalFileSize, fileName);
            System.out.println("total " + totalFileSize);
            userAction.dest = new UserActionResource();
            userAction.dest.id = googledriveid;

            try {
                if (directoryPath.endsWith("/")) {
                    userAction.dest.uri = directoryPath + URLEncoder.encode(fileName, "UTF-8");
                } else {
                    userAction.dest.uri = directoryPath + "/" + URLEncoder.encode(fileName, "UTF-8");
                }

                ObjectMapper mapper = new ObjectMapper();
                userAction.dest.credential = mapper.readValue(credential, UserActionCredential.class);
                IdMap[] idms = mapper.readValue(idmap, IdMap[].class);
                userAction.dest.map = new ArrayList<>(Arrays.asList(idms));
            } catch (Exception e) {
                e.printStackTrace();
            }
            resourceService.submit(cookie, userAction).subscribe();
            return sendFilePart(filePart, uploadQueue).map(size -> {
                if (size < totalFileSize) {
                    ongoingUploads.put(uuid, uploadQueue);
                }
                return size;
            });
        }
    }

    public Mono<Integer> sendFilePart(Mono<FilePart> pfr, LinkedBlockingQueue<Slice> qugue) {
        return pfr.flatMapMany(fp -> fp.content())
                .reduce(new ByteArrayOutputStream(), (acc, newbuf) -> {
                    try {
                        Slice slc = new Slice(newbuf.asByteBuffer());
                        acc.write(slc.asBytes(), 0, slc.length());
                    } catch (Exception e) {
                    }
                    return acc;
                }).map(content -> {
                    System.out.println("uploading" + content.size());
                    Slice slc = new Slice(content.toByteArray());
                    qugue.add(slc);
                    return slc.length();
                });
    }

    public Mono<Void> finishUpload(UUID uuid) {
        if (!ongoingUploads.containsKey(uuid)) {
            return Mono.error(null);
        }
        ongoingUploads.remove(uuid);
        return Mono.just(null);
    }
}
