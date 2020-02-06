package org.onedatashare.server.service;

import org.onedatashare.server.model.core.JobSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class SequenceGeneratorService {


    private MongoOperations mongoOperations;

    @Autowired
    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    /**
     *
     * @param userId (user unique id which is userService.getEmail()
     * @return unique sequence id fetched from jobSequences collection w.r.t a user id
     * findAndModify is an atomic function (thread-safe)
     *
     * this function fetches the jobsequence(total number of jobs) and increment the value by 1 corresponding to userID
     * if exists else it returns and update the jobsequence as 1
     */
    public int generateSequence(String userId) {

        JobSequence counter = mongoOperations.findAndModify(query(where("userId").is(userId)),
                new Update().inc("jobSequence",1), options().returnNew(true).upsert(true),
                JobSequence.class);
        return !Objects.isNull(counter) ? counter.getJobSequence() : 1;

    }
}
