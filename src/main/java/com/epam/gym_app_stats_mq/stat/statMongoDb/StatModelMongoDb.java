package com.epam.gym_app_stats_mq.stat.statMongoDb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "trainer_stats")
@Data
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "lastName_firstName_idx", def = "{'lastName' : 1, 'firstName' : 1}")
})
public class StatModelMongoDb implements Serializable {
    private ObjectId id;
    private String userName;
    private String firstName;
    private String lastName;
    private boolean status;
    private List<YearSummaryMongoDb> trainingSummary;

}
