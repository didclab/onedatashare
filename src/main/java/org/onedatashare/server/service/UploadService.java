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
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

@Service
//TODO: check timeout, size overflow works
//TODO: add check so that ongoing transfers do not crash when ongoing transfers greater than maximum uploads
public class UploadService {

    private static LoadingCache<String, UploadSession> uploadCache;
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private static final int MAXIMUM_UPLOADS = 100;
    private static final int TIMEOUT_SECS = 60;
    private static final int CONCURRENCY_LEVEL = 8;

    static {
        uploadCache = CacheBuilder.newBuilder()
                .maximumSize(MAXIMUM_UPLOADS)
                .expireAfterAccess(TIMEOUT_SECS, TimeUnit.SECONDS)
                .removalListener((RemovalListener<String, UploadSession>) notification ->
                        logger.error(String.format("Upload %s stopped due to %s", notification.getKey(),
                                notification.getCause())))
                .concurrencyLevel(CONCURRENCY_LEVEL)
                 .build(
                        new CacheLoader<String, UploadSession>() {
                            @Override
                            public UploadSession load(String key) {
                                logger.info("Added "+ key);
                                return new UploadSession();
                            }
                        }
                );
    }

    private String createUploadCacheKey(String userId, String directoryPath, String uuid){
        return String.format("%s-%s-%s",userId, directoryPath, uuid);
    }

    public Mono<Slice> sendFilePart(Mono<FilePart> filePartMono){
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
                                     String fileSize, String idMap, String chunkNumber, String totalChunks) {

        long _fileSize;
        int _chunkNumber = 0, _totalChunks=0;
        UserActionCredential _credential;
        IdMap[] _idMap;
        String _pathToWrite = pathToWrite;
        try {
            ObjectMapper mapper = new ObjectMapper();
            _fileSize = Long.parseLong(fileSize);
            if(chunkNumber!=null && totalChunks!=null) {
                _chunkNumber = Integer.parseInt(chunkNumber);
                _totalChunks = Integer.parseInt(totalChunks);
            }
            _credential = mapper.readValue(credential, UserActionCredential.class);
            _idMap = mapper.readValue(idMap, IdMap[].class);
        } catch (IOException e) {
            Mono.error(new Exception("Unable to parse the form data"));
            return Mono.just(false);
        }
        try {
            String slash = "";
            if(pathToWrite.endsWith("/")){
                slash = "/";
            }
            _pathToWrite = String.format("%s%s%s",
                    pathToWrite , slash , URLEncoder.encode(fileName,
                            "utf-8"));
        } catch (UnsupportedEncodingException e) {
            Mono.error(new Exception("Unable to parse the destination path"));
            return Mono.just(false);
        }

        String uploadCacheKey = createUploadCacheKey(userId, pathToWrite, uploadUUID);
        try {
            UploadSession session = uploadCache.get(uploadCacheKey);
            //Check chunk number
            if(_chunkNumber != session.getCurrentChunkNumber()){
                throw new Exception("One or more chunks lost");
            }

//            //Write operation
//            sendFilePart(filePart).map(slice -> {
//
//            });

            //If only one chunk or all chunks received then remove from the map
            if(session.getTotalChunks() == session.getCurrentChunkNumber()){
                uploadCache.invalidate(uploadCacheKey);
            }

            return Mono.just(true);
        }catch (Exception e){
            logger.error("Error " + e.getMessage());
        }

        return Mono.just(false);
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
    private OutputStream outputStream;

    public void write(Slice slice) throws IOException {
        this.currentChunkNumber++;
        outputStream.write(slice.asBytes());
        outputStream.flush();
        this.uploadedBytes += slice.length();
        if(this.uploadedBytes == this.fileSize){
            outputStream.close();
        }
    }
}