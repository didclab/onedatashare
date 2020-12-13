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


package org.onedatashare.server.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.onedatashare.server.model.core.EndpointType;

import java.util.HashSet;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TransferJobRequest {
    @NonNull protected Source source;
    @NonNull protected Destination destination;
    protected TransferOptions options;


    @Data
    @Accessors(chain = true)
    public static class Destination {
        @NonNull protected EndpointType type;
        @NonNull protected String credId;
        @NonNull protected EntityInfo info;
    }

    @Data
    @Accessors(chain = true)
    public static class Source {
        @NonNull protected EndpointType type;
        @NonNull protected String credId;
        @NonNull protected EntityInfo info;
        @NonNull protected HashSet<EntityInfo> infoList;
    }

    @Data
    @Accessors(chain = true)
    public static class EntityInfo {
        protected String id;
        protected String path;
        protected long size;
    }

}