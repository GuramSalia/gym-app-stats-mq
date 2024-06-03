package com.epam.gym_app_stats_mq.stat.statMongoDb;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StatRepoMongoDb extends MongoRepository<StatModelMongoDb, ObjectId> {
    StatModelMongoDb findByUserName(String userName);
}
