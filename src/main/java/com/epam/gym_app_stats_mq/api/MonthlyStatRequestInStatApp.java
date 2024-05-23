package com.epam.gym_app_stats_mq.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class MonthlyStatRequestInStatApp implements Serializable {
    @NotNull
    private Integer trainerId;
    @NotNull
    private Integer year;
    @NotNull
    private Integer month;

    public static MonthlyStatRequestInStatApp fromMap(Map<String, String> map) {
        MonthlyStatRequestInStatApp request = new MonthlyStatRequestInStatApp();
        request.setTrainerId(Integer.parseInt(map.get("trainerId")));
        request.setYear(Integer.parseInt(map.get("year")));
        request.setMonth(Integer.parseInt(map.get("month")));
        return request;
    }
}
