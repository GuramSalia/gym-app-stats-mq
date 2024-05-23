package com.epam.gym_app_stats_mq.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class FullStatRequestInStatApp implements Serializable {
    @NotNull
    private Integer trainerId;

    public static FullStatRequestInStatApp fromMap(Map<String, String> map) {
        FullStatRequestInStatApp request = new FullStatRequestInStatApp();
        request.setTrainerId(Integer.parseInt(map.get("trainerId")));
        return request;
    }
}
