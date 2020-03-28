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


package org.onedatashare.server.model.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import static org.onedatashare.server.model.core.ODSConstants.TRANSFER_SLICE_SIZE;

/**
 * This is used to track the progress of a transfer in real time.
 */
@NoArgsConstructor
@Data
public class TransferInfo {
    /** Units complete. */
    public long done;
    /** Total units. */
    public long total;
    /** Average throughput. */
    public double avg;
    /** Instantaneous throughput. */
    public double inst;

    @Transient
    private long lastTime = Time.now();

    /*Reason for failure */
    public String reason;

    /** Update based on the given information. */
    public void update(Time time, Progress p, Throughput tp) {
        done = p.done();
        avg = p.rate(time).value();
        long currTime = time.elapsed();
        inst = TRANSFER_SLICE_SIZE / (currTime - lastTime);
        lastTime = currTime;
    }

    public TransferInfo(long total) {
        this.total = total;
    }
}
