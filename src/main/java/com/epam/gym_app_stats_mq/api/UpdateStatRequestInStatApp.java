package com.epam.gym_app_stats_mq.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class UpdateStatRequestInStatApp implements Serializable {
    @NotNull
    private Integer trainerId;
    @NotNull
    private Integer year;
    @NotNull
    private Integer month;
    @NotNull
    private Integer duration;
    @NotNull
    private ActionTypeInStatApp actionTypeInStatApp;

    public static UpdateStatRequestInStatApp fromMap(Map<String, String> map) {
        UpdateStatRequestInStatApp request = new UpdateStatRequestInStatApp();
        request.setTrainerId(Integer.parseInt(map.get("trainerId")));
        request.setYear(Integer.parseInt(map.get("year")));
        request.setMonth(Integer.parseInt(map.get("month")));
        request.setDuration(Integer.parseInt(map.get("duration")));
        request.setActionTypeInStatApp(ActionTypeInStatApp.valueOf(map.get("actionType")));
        return request;
    }
}
