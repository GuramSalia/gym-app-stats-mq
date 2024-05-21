package com.epam.gym_app_stats_mq.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatRequest {
    @NotNull
    private Integer trainerId;
    @NotNull
    private Integer year;
    @NotNull
    private Integer month;
    @NotNull
    private Integer duration;
    @NotNull
    private ActionType actionType;
    @NotNull
    private String token;
}
