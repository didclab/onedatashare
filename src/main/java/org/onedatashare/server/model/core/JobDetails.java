package org.onedatashare.server.model.core;

import lombok.Data;

import java.util.List;

@Data
public class JobDetails{

    private List<Job> jobs;
    private Long totalCount;
}
