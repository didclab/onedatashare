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


package org.onedatashare.server.model.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model to hold response of queue and history pages.
 *
 * Holds list of jobs for the current page number (pagination) on queue page the user is viewing
 * and the total count of jobs related to the user to generate page numbers.
 * (totalCount holds the count of all jobs in the database for history page)
 */
@Data
public class JobDetails{

    private Long totalCount;
    private List<Job> jobs;

    public JobDetails(List<Job> jobs, Long totalCount) {
        this.jobs = jobs;
        this.totalCount = totalCount;
    }
}
