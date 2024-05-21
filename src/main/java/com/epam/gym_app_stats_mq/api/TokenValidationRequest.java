package com.epam.gym_app_stats_mq.api;

import lombok.Data;

@Data
public class TokenValidationRequest {
    private String token;

    public TokenValidationRequest(String token) {
        this.token = token;
    }

    public TokenValidationRequest() {}
}
