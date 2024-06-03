package com.epam.gym_app_stats_mq.stat.statMongoDb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearSummaryMongoDb implements Serializable {
    private String year;
    private List<MonthlySummaryMongoDb> months;
}
