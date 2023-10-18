/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */


package org.onedatashare.server.model.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserTransferOptions {
    private Boolean compress; //implemented
    private Boolean encrypt; //we currently need to add FTPS, HTTPS support I believe
    private String optimizer; //not yet supported, might use B.O maybe?
    private Boolean overwrite;  //adding this next
    private Integer retry; //supported
    private Boolean verify; //not supported
    private Integer concurrencyThreadCount; //supported
    private Integer parallelThreadCount; //supported
    private Integer pipeSize; //supported
    private Integer chunkSize; //supported
    private LocalDateTime scheduledTime;

    public UserTransferOptions() {
        this.compress = false;
        this.encrypt = false;
        this.optimizer = "";
        this.overwrite = false;
        this.retry = 0;
        this.verify = false;
        this.concurrencyThreadCount = 1;
        this.pipeSize = 1;
        this.parallelThreadCount = 1;
        this.chunkSize = 10 << 1024 << 1024;
        this.scheduledTime = LocalDateTime.now();
    }
}
