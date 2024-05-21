package com.epam.gym_app_stats_mq.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorDetails {
    private final HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime localDateTime;
    private final String message;
    private final String details;

    public ErrorDetails(
            @NonNull HttpStatus status,
            @NonNull String exMessage,
            String details) {
        this.localDateTime = LocalDateTime.now();
        this.status = status;
        this.message = exMessage;
        this.details = details;
    }

    @Override
    public String toString() {
        return "ErrorDetails{" +
                ", status=" + status +
                ", localDateTime=" + localDateTime +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
