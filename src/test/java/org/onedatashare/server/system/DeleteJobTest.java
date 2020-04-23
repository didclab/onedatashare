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

package org.onedatashare.server.system;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.controller.DeleteJobController;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.request.JobRequestData;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.repository.JobRepository;
import org.onedatashare.server.system.base.SystemTest;
import org.onedatashare.server.system.mockuser.WithMockCustomUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

/**
 * A system test suite that tests the deletion of {@link Job}s by their owner
 * <br><br>
 * Entry point for requests: {@link DeleteJobController}
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class DeleteJobTest extends SystemTest {

    private static final String DELETE_JOB_URL = "/api/stork/deleteJob";

    @MockBean
    private JobRepository jobRepository;

    private Map<UUID, Job> jobs = new HashMap<>();

    @Before
    public void setup() {
        super.setup();
        when(jobRepository.insert((Job)any())).thenAnswer(addToJobs());
        when(jobRepository.save(any())).thenAnswer(updateJob());
        when(jobRepository.findById((UUID) any())).thenAnswer(findJobById());

        jobs.clear();
        users.clear();
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenJobAlreadyExists_WhenRequestingToDelete_ShouldMarkAsDeleted() throws Exception {
        int jobId = 0;
        Job job = jobOf(TEST_USER_EMAIL, jobId, "src0", "dest0");
        jobRepository.insert(job);
        userRepository.insert(userWithJobs(TEST_USER_EMAIL, job));

        JobRequestData jobRequestData = jobRequestDataOf(jobId);
        processPostWithRequestData(DELETE_JOB_URL, jobRequestData).andReturn().getAsyncResult(1000);

        assertTrue(jobs.get(job.getUuid()).isDeleted());
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenTwoUsersHaveDifferentJobsWithSameId_WhenRequestingToDelete_ShouldOnlyDeleteLoggedInUserJob()
            throws Exception {
        int jobId = 0;
        Job job = jobOf(TEST_USER_EMAIL, jobId, "src0", "dest0");
        jobRepository.insert(job);
        userRepository.insert(userWithJobs(TEST_USER_EMAIL, job));
        String otherUser = "otheruser@ot.us";
        Job otherJob = jobOf(otherUser, jobId, "src0", "dest0");
        jobRepository.insert(otherJob);
        userRepository.insert(userWithJobs(otherUser, otherJob));

        JobRequestData jobRequestData = jobRequestDataOf(jobId);
        processPostWithRequestData(DELETE_JOB_URL, jobRequestData).andReturn().getAsyncResult(1000);

        assertTrue(jobs.get(job.getUuid()).isDeleted());
        assertFalse(jobs.get(otherJob.getUuid()).isDeleted());
    }

    private User userWithJobs(String email, Job... userJobs) {
        User user = new User();
        user.setEmail(email);
        user.setJobs(new HashSet<>(getJobIds(userJobs)));
        return user;
    }

    private JobRequestData jobRequestDataOf(int job_id) {
        JobRequestData jobRequestData = new JobRequestData();
        jobRequestData.setJob_id(job_id);
        return jobRequestData;
    }

    private Answer<Mono<Job>> updateJob() {
        return invocationOnMock -> {
            Job job = invocationOnMock.getArgument(0);
            jobs.put(job.getUuid(), job);
            return just(job);
        };
    }

    private List<UUID> getJobIds(Job... jobs) {
        List<UUID> ids = new ArrayList<>();
        for(Job job : jobs)
            ids.add(job.getUuid());
        return ids;
    }

    private Job jobOf(String ownerEmail, int jobId, String fromUri, String toUri) {
        UserActionResource fromRsrc = userActionResourceOf(fromUri);
        UserActionResource toRsrc = userActionResourceOf(toUri);
        Job j = new Job(fromRsrc, toRsrc);
        j.setOwner(ownerEmail);
        j.setJob_id(jobId);
        return j;
    }

    private UserActionResource userActionResourceOf(String toUri) {
        UserActionResource rsrc = new UserActionResource();
        rsrc.setUri(toUri);
        return rsrc;
    }

    private Answer<Mono<Job>> addToJobs() {
        return invocation -> {
            Job job = invocation.getArgument(0);
            jobs.put(job.getUuid(), job);
            return just(job);
        };
    }

    private Answer<Mono<Job>> findJobById() {
        return invocationOnMock -> {
            UUID uuid = invocationOnMock.getArgument(0);
            return Mono.just(jobs.get(uuid));
        };
    }
}
