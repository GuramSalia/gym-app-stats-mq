package com.epam.gym_app_stats_mq.stat.statMongoDb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummaryMongoDb implements Serializable {
    private String month;
    private Integer trainingSummaryDuration;
}
