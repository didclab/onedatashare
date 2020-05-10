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
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.onedatashare.server.model.core.EndpointType;

import java.util.List;

@Data
@Accessors(chain = true)
public class TransferJobRequest {
    @NonNull
    private String id;
    @NonNull private Source source;
    @NonNull private Destination destination;
    private TransferOptions options;


    @Data
    @Accessors(chain = true)
    public static class Destination {
        @NonNull private EndpointType type;
        @NonNull private String credId;
        @NonNull private EntityInfo info;
    }

    @Data
    @Accessors(chain = true)
    public static class Source {
        @NonNull private EndpointType type;
        @NonNull private String credId;
        @NonNull private EntityInfo info;
        @NonNull private List<EntityInfo> infoList;
    }

    @Data
    @Accessors(chain = true)
    public static class EntityInfo {
        private String id;
        private String path;
        private long size;
    }

}