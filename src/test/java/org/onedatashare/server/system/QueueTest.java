package org.onedatashare.server.system;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.controller.QueueController;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.repository.JobRepository;
import org.onedatashare.server.system.base.SystemTest;
import org.onedatashare.server.system.mockuser.WithMockCustomUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.just;

/**
 * A system test suite that tests actions on the queue of user jobs
 * <br><br>
 * Entry point for requests: {@link QueueController}
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class QueueTest extends SystemTest {

    private static final String QUEUE_BASE_URL = "/api/v1/stork/q";
    private static final String GET_USER_JOBS_URL = QUEUE_BASE_URL + "/user-jobs";
    private static final String GET_ALL_USER_JOBS_URL = QUEUE_BASE_URL + "/admin-jobs";
    private static final String GET_USER_JOB_UPDATES_URL = QUEUE_BASE_URL + "/update-user-jobs";
    private static final String GET_ALL_JOB_UPDATES_URL = QUEUE_BASE_URL + "/update-admin-jobs";

    @MockBean
    private JobRepository jobRepository;

    private Map<UUID, Job> jobs = new HashMap<>();

    @Before
    public void setup() {
        when(jobRepository.insert((Job)any())).thenAnswer(addToJobs());
        when(jobRepository.findJobsForUser(any(), any())).thenAnswer(getJobsByEmail());
        when(jobRepository.getJobCountForUser(any())).thenAnswer(getUserJobCount());
        when(jobRepository.getJobCountForUser(any())).thenAnswer(getUserJobCount());
        when(jobRepository.findAllBy(any())).thenAnswer(getAllJobs());
        when(jobRepository.getCount()).thenAnswer(getAllJobsCount());
        when(jobRepository.findAllById((List<UUID>) any())).thenAnswer(getAllJobsByIds());

        jobs.clear();
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenLoggedInUserHasJobs_WhenRequestingJobsForUser_ShouldOnlyReturnJobsForLoggedInInUser() throws Exception {
        JobRequest jobRequest = emailSortedJobRequest();
        Job job1 = jobOf(TEST_USER_EMAIL, "src0", "dest0");
        Job job2 = jobOf(TEST_USER_EMAIL, "src1", "dest1");
        Job job3 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        jobRepository.insert(job1);
        jobRepository.insert(job2);
        jobRepository.insert(job3);

        JobDetails jobDetails = (JobDetails) processPostWithRequestData(GET_USER_JOBS_URL, jobRequest)
                .andReturn().getAsyncResult();

        assertEquals(3, jobDetails.getJobs().size());
        assertListsEqualAsSets(asList(job1, job2, job3), jobDetails.getJobs());
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenMultipleUsersHaveMultipleJobs_WhenRequestingJobsForUser_ShouldOnlyReturnJobsForLoggedInInUser() throws Exception {
        JobRequest jobRequest = emailSortedJobRequest();
        String otherUserEmail = "otheruser@othercorp@oth";
        Job job1 = jobOf(TEST_USER_EMAIL, "src0", "dest0");
        Job job2 = jobOf(TEST_USER_EMAIL, "src1", "dest1");
        Job job3 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        Job otherJob1 = jobOf(otherUserEmail, "src0", "dest0");
        Job otherJob2 = jobOf(otherUserEmail, "src1", "dest1");

        jobRepository.insert(job1);
        jobRepository.insert(job2);
        jobRepository.insert(job3);
        jobRepository.insert(otherJob1);
        jobRepository.insert(otherJob2);

        JobDetails jobDetails = (JobDetails) processPostWithRequestData(GET_USER_JOBS_URL, jobRequest)
                .andReturn().getAsyncResult();

        assertEquals(3, jobDetails.getJobs().size());
        assertListsEqualAsSets(asList(job1, job2, job3), jobDetails.getJobs());
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.ADMIN)
    public void givenMultipleUsersHaveMultipleJobs_WhenRequestingAllJobsAsAdmin_ShouldReturnAllJobsForAllUsers() throws Exception {
        JobRequest jobRequest = emailSortedJobRequest();
        String otherUserEmail = "otheruser@othercorp@oth";
        Job job1 = jobOf(TEST_USER_EMAIL, "src0", "dest0");
        Job job2 = jobOf(TEST_USER_EMAIL, "src1", "dest1");
        Job job3 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        Job otherJob1 = jobOf(otherUserEmail, "src0", "dest0");
        Job otherJob2 = jobOf(otherUserEmail, "src1", "dest1");

        jobRepository.insert(job1);
        jobRepository.insert(job2);
        jobRepository.insert(job3);
        jobRepository.insert(otherJob1);
        jobRepository.insert(otherJob2);

        JobDetails jobDetails = (JobDetails) processPostWithRequestData(GET_ALL_USER_JOBS_URL, jobRequest)
                .andReturn().getAsyncResult();

        assertEquals(5, jobDetails.getJobs().size());
        assertListsEqualAsSets(asList(job1, job2, job3, otherJob1, otherJob2), jobDetails.getJobs());
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenAnyContext_WhenRequestingAllJobsAsNonAdmin_ShouldFail() throws Exception {
        JobRequest jobRequest = emailSortedJobRequest();

        Object response = processPostWithRequestData(GET_ALL_USER_JOBS_URL, jobRequest)
                .andReturn().getAsyncResult();

        assertTrue(response instanceof AccessDeniedException);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserHasJobs_WhenRequestingSpecificJobs_ShouldOnlyReturnJobsForUserThatHaveNotBeenDeleted()
            throws Exception {
        String otherUserEmail = "otheruser@othercorp@oth";
        Job job1 = jobOf(TEST_USER_EMAIL, "src0", "dest0");
        Job job2 = jobOf(TEST_USER_EMAIL, "src1", "dest1");
        Job job3 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        Job job4 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        Job otherJob1 = jobOf(otherUserEmail, "src2", "dest2");
        Job otherJob2 = jobOf(otherUserEmail, "src2", "dest2");
        job3.setDeleted(true);
        otherJob2.setDeleted(true);
        jobRepository.insert(job1);
        jobRepository.insert(job2);
        jobRepository.insert(job3);
        jobRepository.insert(job4);
        jobRepository.insert(otherJob1);
        jobRepository.insert(otherJob2);

        List<UUID> requestedJobs = getJobIds(job1, job2, job3, job4, otherJob1, otherJob2);
        List<Job> fetchedJobs = (List<Job>) processPostWithRequestData(GET_USER_JOB_UPDATES_URL, requestedJobs)
                .andReturn().getAsyncResult();

        assertEquals(3, fetchedJobs.size());
        assertListsEqualAsSets(asList(job1, job2, job4), fetchedJobs);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.ADMIN)
    public void givenMultipleUserHaveMultipleJobs_WhenRequestingSpecificJobsAsAdmin_ShouldReturnAllRequestedJobs()
            throws Exception {
        String otherUserEmail = "otheruser@othercorp@oth";
        Job job1 = jobOf(TEST_USER_EMAIL, "src0", "dest0");
        Job job2 = jobOf(TEST_USER_EMAIL, "src1", "dest1");
        Job job3 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        Job job4 = jobOf(TEST_USER_EMAIL, "src2", "dest2");
        Job otherJob1 = jobOf(otherUserEmail, "src2", "dest2");
        Job otherJob2 = jobOf(otherUserEmail, "src2", "dest2");
        job3.setDeleted(true);
        otherJob2.setDeleted(true);
        jobRepository.insert(job1);
        jobRepository.insert(job2);
        jobRepository.insert(job3);
        jobRepository.insert(job4);
        jobRepository.insert(otherJob1);
        jobRepository.insert(otherJob2);

        List<UUID> requestedJobs = getJobIds(job1, job2, job3, job4, otherJob1, otherJob2);
        List<Job> fetchedJobs = (List<Job>) processPostWithRequestData(GET_ALL_JOB_UPDATES_URL, requestedJobs)
                .andReturn().getAsyncResult();

        assertEquals(6, fetchedJobs.size());
        assertListsEqualAsSets(asList(job1, job2, job3, job4, otherJob1, otherJob2), fetchedJobs);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenAnyContext_WhenRequestingSpecificJobsThroughAdminEndpointAsNonAdmin_ShouldFail()
            throws Exception {
        Job job1 = jobOf(TEST_USER_EMAIL, "src0", "dest0");
        jobRepository.insert(job1);

        List<UUID> requestedJobs = getJobIds(job1);
        Object response = processPostWithRequestData(GET_ALL_JOB_UPDATES_URL, requestedJobs)
                .andReturn().getAsyncResult();

        assertTrue(response instanceof AccessDeniedException);
    }

    private List<UUID> getJobIds(Job... jobs) {
        List<UUID> ids = new ArrayList<>();
        for(Job job : jobs)
            ids.add(job.getUuid());
        return ids;
    }

    private <T> void assertListsEqualAsSets(List<T> list1, List<T> list2) {
        assertEquals(new HashSet<>(list1), new HashSet<>(list2));
    }

    private Job jobOf(String ownerEmail, String fromUri, String toUri) {
        UserActionResource fromRsrc = userActionResourceOf(fromUri);
        UserActionResource toRsrc = userActionResourceOf(toUri);
        Job j = new Job(fromRsrc, toRsrc);
        j.setOwner(ownerEmail);
        return j;
    }

    private UserActionResource userActionResourceOf(String toUri) {
        UserActionResource rsrc = new UserActionResource();
        rsrc.setUri(toUri);
        return rsrc;
    }

    private JobRequest emailSortedJobRequest() {
        JobRequest jobRequest = new JobRequest();
        jobRequest.setPageNo(1);
        jobRequest.setPageSize(1);
        jobRequest.setSortBy("email");
        jobRequest.setSortOrder("asc");
        return jobRequest;
    }

    private Answer<Mono<Job>> addToJobs() {
        return invocation -> {
            Job job = invocation.getArgument(0);
            jobs.put(job.getUuid(), job);
            return just(job);
        };
    }

    private Answer<Flux<Job>> getJobsByEmail() {
        return invocationOnMock -> {
            String ownerEmail = invocationOnMock.getArgument(0);
            return fromIterable(getJobsByEmail(ownerEmail));
        };
    }

    private Answer<Mono<Long>> getUserJobCount() {
        return invocationOnMock -> {
            String ownerEmail = invocationOnMock.getArgument(0);
            return just((long) getJobsByEmail(ownerEmail).size());
        };
    }

    private List<Job> getJobsByEmail(String ownerEmail) {
        return jobs.values().stream()
                .filter(j -> j.getOwner().equals(ownerEmail))
                .collect(toList());
    }

    private Answer<Flux<Job>> getAllJobs() {
        return invocationOnMock -> fromIterable(jobs.values());
    }

    private Answer<Flux<Job>> getAllJobsByIds() {
        return invocationOnMock -> {
            List<UUID> requestedJobs = invocationOnMock.getArgument(0);
            return fromIterable(jobs.entrySet().stream()
                    .filter(entry -> requestedJobs.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(toList()));
        };
    }

    private Answer<Mono<Long>> getAllJobsCount() {
        return invocationOnMock -> Mono.just((long)jobs.size());
    }
}
