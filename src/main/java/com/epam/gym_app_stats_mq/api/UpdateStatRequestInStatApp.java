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
    @NotNull
    private String userName;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private Boolean status;

    public static UpdateStatRequestInStatApp fromMap(Map<String, String> map) {
        UpdateStatRequestInStatApp request = new UpdateStatRequestInStatApp();
        request.setTrainerId(Integer.parseInt(map.get("trainerId")));
        request.setYear(Integer.parseInt(map.get("year")));
        request.setMonth(Integer.parseInt(map.get("month")));
        request.setDuration(Integer.parseInt(map.get("duration")));
        request.setActionTypeInStatApp(ActionTypeInStatApp.valueOf(map.get("actionType")));
        request.setUserName(map.get("userName"));
        request.setFirstName(map.get("firstName"));
        request.setLastName(map.get("lastName"));
        request.setStatus(Boolean.parseBoolean(map.get("status")));
        return request;
    }
}
