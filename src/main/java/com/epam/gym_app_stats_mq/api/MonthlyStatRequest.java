package com.epam.gym_app_stats_mq.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthlyStatRequest {
    @NotNull
    private Integer trainerId;
    @NotNull
    private Integer year;
    @NotNull
    private Integer month;
    @NotNull
    private String token;
}
