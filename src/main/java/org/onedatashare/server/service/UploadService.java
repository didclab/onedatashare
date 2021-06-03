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
import lombok.Setter;
import lombok.experimental.Accessors;
import org.onedatashare.server.model.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

import java.io.ByteArrayOutputStream;
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


    public Mono<Slice> createSlice(Mono<FilePart> filePartMono) {
        return filePartMono.flatMapMany(fp -> fp.content())
                .reduce(new ByteArrayOutputStream(), (acc, newbuf) -> {
                    try {
                        Slice slc = new Slice(newbuf.asByteBuffer());
                        acc.write(slc.asBytes(), 0, slc.length());
                    } catch (Exception e) {
                    }
                    return acc;
                }).map(content -> {
                    logger.debug("uploading " + content.size());
                    Slice slc = new Slice(content.toByteArray());
                    return slc;
                });
    }
}

@Accessors(chain = true)
final class UploadSession{
    @Getter private int currentChunkNumber = 0;
    @Getter private int totalChunks;
    private long fileSize;
    private long uploadedBytes;

    public UploadSession(int totalChunks, long fileSize) {
        this.totalChunks = totalChunks;
        this.fileSize = fileSize;
    }
}