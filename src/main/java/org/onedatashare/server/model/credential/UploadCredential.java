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


package org.onedatashare.server.model.credential;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Slice;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class UploadCredential extends Credential {
    @Transient
    private LinkedBlockingQueue<Slice> fux;
    @Transient
    private String name;
    @Transient
    private Long size;
    @Transient
    private Mono<FilePart> _no1;

    public UploadCredential(LinkedBlockingQueue<Slice> fax, Long _size, String _name){
        fux = fax;
        name = _name;
        size = _size;
    }
}