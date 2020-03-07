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
