package com.epam.gym_app_stats_mq.stat.statMongoDb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
@Profile("dev")
public class MongoDBInitializer {
    @Bean
    public CommandLineRunner init(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.dropCollection("trainer_stats");
            mongoTemplate.createCollection("trainer_stats");

            IndexOperations indexOps = mongoTemplate.indexOps("trainer_stats");
            Index index = new Index().on("lastName", Sort.Direction.ASC).on("firstName", Sort.Direction.ASC);
            indexOps.ensureIndex(index);

            StatModelMongoDb timSmith = new StatModelMongoDb(
                    null,
                    "Tim.Smith",
                    "Tim",
                    "Smith",
                    true,
                    new ArrayList<>(Arrays.asList(
                            new YearSummaryMongoDb("2023", new ArrayList<>(Arrays.asList(
                                    new MonthlySummaryMongoDb("12", 600)
                            ))),
                            new YearSummaryMongoDb("2024", new ArrayList<>(Arrays.asList(
                                    new MonthlySummaryMongoDb("1", 300),
                                    new MonthlySummaryMongoDb("2", 60),
                                    new MonthlySummaryMongoDb("3", 2400)
                            )))
                    ))
            );

            StatModelMongoDb samJones = new StatModelMongoDb(
                    null,
                    "Sam.Jones",
                    "Sam",
                    "Jones",
                    true,
                    new ArrayList<>(Arrays.asList(
                            new YearSummaryMongoDb("2022", new ArrayList<>(Arrays.asList(
                                    new MonthlySummaryMongoDb("7", 45)
                            ))),
                            new YearSummaryMongoDb("2024", new ArrayList<>(Arrays.asList(
                                    new MonthlySummaryMongoDb("2", 45)
                            )))
                    ))
            );

            mongoTemplate.insert(timSmith, "trainer_stats");
            mongoTemplate.insert(samJones, "trainer_stats");
        };
    }
}
