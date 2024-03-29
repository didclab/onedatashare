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

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TransferJobRequestWithMetaData{
    private String ownerId;

    @NonNull protected TransferJobRequest.Source source;
    @NonNull protected TransferJobRequest.Destination destination;
    protected UserTransferOptions options;

    public static TransferJobRequestWithMetaData getTransferRequestWithMetaData(String owner,
                                                                                TransferJobRequest request){
        TransferJobRequestWithMetaData requestWithMetaData = new TransferJobRequestWithMetaData();
        requestWithMetaData.ownerId = owner;
        requestWithMetaData.source = request.getSource();
        requestWithMetaData.destination = request.getDestination();
        requestWithMetaData.options = request.getOptions();
        return requestWithMetaData;
    }
}