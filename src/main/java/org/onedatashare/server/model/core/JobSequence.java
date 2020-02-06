package org.onedatashare.server.model.core;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jobsequences")
@Data
public class JobSequence {

    @Id
    private String userId;

    private int jobSequence;
}
