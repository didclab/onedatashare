package org.onedatashare.server.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onedatashare.server.model.requestdata.BatchJobData;
import org.onedatashare.server.model.response.PageImplResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MetaDataServiceTest {

    MetaDataService testObj;

    @BeforeEach
    public void beforeEach() {
        RestClient.Builder builder = RestClient.builder();
        testObj = new MetaDataService(builder);
        ReflectionTestUtils.setField(testObj, "metaHostName", "http://localhost:8084");
    }

    @Test
    public void testListJobIdsEmptyEmail() {
        List<Long> jobIds = testObj.getAllJobIds("");
        Assertions.assertTrue(jobIds.isEmpty());
    }

    @Test
    public void testListJobIdsValidEmail() {
        List<Long> jobIds = testObj.getAllJobIds("jgoldverg@gmail.com");
        Assertions.assertFalse(jobIds.isEmpty());
    }

    @Test
    public void testGetAllJobStatsEmpty() {
        List<BatchJobData> emptyList = testObj.getAllStats("");
        Assertions.assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testGetAllJobStatsValidEmail() {
        String ownerId = "jgoldverg@gmail.com";
        List<BatchJobData> userJobList = testObj.getAllStats(ownerId);
        Assertions.assertFalse(userJobList.isEmpty());
        for (BatchJobData batchJobData : userJobList) {
            Assertions.assertEquals(batchJobData.getJobParameters().get("ownerId"), ownerId);
        }
    }

    @Test
    public void testGetUserUUIDSEmptyEmail() {
        String ownerId = "";
        List<UUID> userUuids = testObj.getUserUuids(ownerId);
        Assertions.assertTrue(userUuids.isEmpty());
    }

    @Test
    public void testGetUserUUIDSValidEmail() {
        String ownerId = "jgoldverg@gmail.com";
        List<UUID> userUuids = testObj.getUserUuids(ownerId);
        Assertions.assertFalse(userUuids.isEmpty());
    }

    @Test
    public void testGetUserJobByUUID() {
        UUID uuid = UUID.randomUUID();
        BatchJobData batchJobData = testObj.getUserJobByUuid(uuid);
        Assertions.assertNull(batchJobData.getId());
    }

    @Test
    public void testUuidVsJobExecutionId() {
        String ownerId = "jgoldverg@gmail.com";
        UUID jobUuid = testObj.getUserUuids(ownerId).getFirst();
        BatchJobData jobData = testObj.getUserJobByUuid(jobUuid);
        BatchJobData jobDataId = testObj.getJobStat(jobData.getId());
        Assertions.assertEquals(jobData, jobDataId);
    }

    @Test
    public void testJobIdvsUuid() {
        String ownerId = "jgoldverg@gmail.com";
        Long jobId = testObj.getAllJobIds(ownerId).getLast();
        BatchJobData jobIdStat = testObj.getJobStat(jobId);
        UUID uuid = UUID.fromString(jobIdStat.getJobParameters().get("jobUuid"));
        BatchJobData uuidStat = testObj.getUserJobByUuid(uuid);
        Assertions.assertEquals(jobIdStat, uuidStat);
        Assertions.assertEquals(uuidStat.getId(), jobIdStat.getId());
    }

    @Test
    public void testPageAbleJobStatsPageZero() {
        String ownerId = "jgoldverg@gmail.com";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("jobInstanceId").ascending());

        PageImplResponse<BatchJobData> resp = testObj.getAllStats(ownerId, pageable);
        List<BatchJobData> jobDataList = resp.get().toList();
        Assertions.assertEquals(10, resp.get().toList().size());
        for(int i = 0; i < resp.getSize()-1; i++){
            BatchJobData current = jobDataList.get(i);
            Assertions.assertTrue(current.getId() < jobDataList.get(i+1).getId());
            System.out.println(current.getId());
        }
    }

    @Test
    public void testPageAbleJobStatsPageOne(){
        String ownerId = "jgoldverg@gmail.com";
        Pageable pageable = PageRequest.of(1, 10, Sort.by("jobInstanceId").ascending());

        PageImplResponse<BatchJobData> resp = testObj.getAllStats(ownerId, pageable);
        List<BatchJobData> jobDataList = resp.get().toList();
        Assertions.assertEquals(10, resp.get().toList().size());
        for(int i = 0; i < resp.getSize()-1; i++){
            BatchJobData current = jobDataList.get(i);
            Assertions.assertTrue(current.getId() < jobDataList.get(i+1).getId());
            System.out.println(current.getId());
            System.out.println(current.getStatus());
        }

    }

    @Test
    public void queryRunningJobsNoneHere(){
        String ownerId = "";
        String status = "completed";
        List<BatchJobData> jobData = testObj.queryRunningJobs(ownerId, status);
        Assertions.assertTrue(jobData.isEmpty());
    }

    @Test
    public void queryRunningJobsCompleted(){
        String ownerId = "jgoldverg@gmail.com";
        String status = "completed";
        List<BatchJobData> jobData = testObj.queryRunningJobs(ownerId, status.toUpperCase());
        for(BatchJobData jobData1: jobData){
            System.out.println(jobData1.toString());
        }
        Assertions.assertFalse(jobData.isEmpty());
    }
}
