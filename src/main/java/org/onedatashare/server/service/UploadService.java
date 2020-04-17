/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.service;

import com.google.common.cache.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

@Service
//TODO: check timeout, size overflow works
//TODO: add check so that ongoing transfers do not crash when ongoing transfers greater than maximum uploads
public class UploadService {

    private static Cache<String, UploadSession> uploadCache;
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private static final int MAXIMUM_UPLOAD_LIMIT = 100;
    private static final int TIMEOUT_SECS = 60;
    private static final int CONCURRENCY_LEVEL = 8;

    static {
        uploadCache = CacheBuilder.newBuilder()
                .maximumSize(MAXIMUM_UPLOAD_LIMIT)
                .expireAfterAccess(TIMEOUT_SECS, TimeUnit.SECONDS)
                .removalListener((RemovalListener<String, UploadSession>) notification ->
                        logger.error(String.format("Upload %s stopped due to %s", notification.getKey(),
                                notification.getCause())))
                .concurrencyLevel(CONCURRENCY_LEVEL)
                .build();
    }

    @Autowired
    private VfsService vfsService;

    private String createUploadCacheKey(String userId, String directoryPath, String uuid){
        return String.format("%s-%s-%s",userId, directoryPath, uuid);
    }

    public Mono<Slice> createSlice(Mono<FilePart> filePartMono){
        return filePartMono.flatMapMany(fp -> fp.content())
                .reduce(new ByteArrayOutputStream(), (acc, newbuf)->{
                    try
                    {
                        Slice slc = new Slice(newbuf.asByteBuffer());
                        acc.write(slc.asBytes(), 0, slc.length());
                    }catch(Exception e){}
                    return acc;
                }).map(content ->  {
                    logger.debug("uploading " + content.size());
                    Slice slc = new Slice(content.toByteArray());
                    return slc;
                });
    }

    public Mono<Boolean> uploadChunk(String userId, String uploadUUID, Mono<FilePart> filePart,
                                     String credential, String pathToWrite, String fileName,
                                     String fileSize, String idMap, String chunkNumber, String totalChunks,
                                     String destFolderId) {
        //Processing
        long _fileSize = Long.parseLong(fileSize);;
        int _chunkNumber = chunkNumber == null ? 0 : Integer.parseInt(chunkNumber);
        long _totalChunks = totalChunks == null ? 0 : Long.parseLong(totalChunks);
        UserActionCredential _credential;
        IdMap[] _idMap;
        String _pathToWrite = pathToWrite;
        try {
            fileName = URLEncoder.encode(fileName, "utf-8");
            pathToWrite = "ftp://localhost:2121/temp/";
            _pathToWrite = pathToWrite.endsWith("/") ? pathToWrite + fileName : pathToWrite + "/" + fileName;
            ObjectMapper mapper = new ObjectMapper();
            _credential = mapper.readValue(credential, UserActionCredential.class);
            _idMap = mapper.readValue(idMap, IdMap[].class);
        } catch (IOException e) {
            Mono.error(new Exception("Unable to parse the form data"));
            return Mono.just(false);
        }

        String uploadCacheKey = createUploadCacheKey(userId, _pathToWrite, uploadUUID);
        try {
            UploadSession tempSession = uploadCache.getIfPresent(uploadCacheKey);
            //Issues can still arise due to race conditions. Also, only returns the approximate size .TODO: fix
            if(tempSession == null && uploadCache.size() < MAXIMUM_UPLOAD_LIMIT){
                tempSession = new UploadSession();
                uploadCache.put(uploadCacheKey, tempSession);
            }
            UploadSession session = tempSession;
            //Check chunk number
            if(_chunkNumber != session.getCurrentChunkNumber()){
                throw new Exception("One or more chunks lost");
            }
            if(session.getCurrentChunkNumber() == 0){
                UserAction userAction = new UserAction().setUri(_pathToWrite).setCredential(_credential);
                Mono<Drain> drainMono = vfsService.getResourceWithUserActionUri(null, userAction)
                        .map(Resource::sink);
                return writeFirstChunk(filePart, session, drainMono);
            }
            else {
                return writeSubsequentChunks(filePart, session, uploadCacheKey, _chunkNumber);
            }
        }catch (Exception e){
            logger.error("Error " + e.getMessage());
        }
        return Mono.just(false);
    }

    private Mono<Boolean> writeFirstChunk(Mono<FilePart> filePart, UploadSession session, Mono<Drain> drainMono){
        logger.debug("Wrote 1st chunk");
        return drainMono.flatMap(d -> {
            session.setDrain(d);
            return createSlice(filePart)
                    .map(slice -> {
                        try {
                            session.write(slice, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    });
        });
    }

    private Mono<Boolean> writeSubsequentChunks(Mono<FilePart> filePart, UploadSession session, String uploadCacheKey,
                                                int _chunkNumber){
        logger.debug("Wrote other chunks");
        return createSlice(filePart)
                .map(slice -> {
                    try {
                        session.write(slice, _chunkNumber);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    logger.debug("Wrote other chunks success");
                    return true;
                })
                .doOnSuccess(s -> {
                    //If only one chunk or all chunks received then remove from the map
                    if(session.getTotalChunks() == session.getCurrentChunkNumber()){
                        uploadCache.invalidate(uploadCacheKey);
                    }
                });
    }
}

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
final class UploadSession{
    private int currentChunkNumber = 0;
    private int totalChunks;
    private long fileSize;
    private long uploadedBytes;
    private String url;
    private Drain drain;

    public void write(Slice slice, int chunkNumber) throws Exception {
        if(this.currentChunkNumber != chunkNumber)
            throw new Exception("Missed one or more chunks");
        drain.drain(slice);
        this.currentChunkNumber++;
        this.uploadedBytes += slice.length();
    }
}