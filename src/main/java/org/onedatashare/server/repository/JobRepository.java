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


package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends ReactiveMongoRepository<Job, UUID> {

    String fieldFilter = "{'status' : 1, 'bytes' : 1, 'job_id' : 1, 'owner' : 1, 'times' : 1, 'src.uri' : 1, 'dest.uri' : 1}";

    @Query(value="{$and: [{'owner':?0},{'deleted': false}]}", fields = fieldFilter)
    Flux<Job> findJobsForUser(String owner, Pageable pageable);

    @Query(value = "{$and: [{'owner':{ $regex: '?0', $options:'i' }}, {'status':{ $regex: '?1', $options:'i' }}, " +
            "{'job_id' : {$gt: '?2', $lt: '?3'}}, {'deleted': false}]}", fields = fieldFilter)
    Flux<Job> findSearchJobs(String owner, String status, int start, int end, Pageable pageable);

    @Query(value = "{'deleted' : false}", fields = fieldFilter)
    Flux<Job> findAllBy(Pageable pageable);

    @Query(value="{$and: [{'owner':?0},{'deleted': false}]}", count = true)
    Mono<Long> getJobCountForUser(String owner);

    @Query(value = "{$and: [{'owner': { $regex: ?0, $options:'i' }}, {'status':{ $regex: '?1', $options:'i' }}, {'job_id' : {$lt: '?3'}}, {'deleted': false}]}", count = true)
    Mono<Long> getSearchJobsCount(String owner, String status, int start, int end);

    @Query(value = "{'deleted': false}", count = true)
    Mono<Long> getCount();
}
