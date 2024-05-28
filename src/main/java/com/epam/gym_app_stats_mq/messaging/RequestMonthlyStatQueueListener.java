package com.epam.gym_app_stats_mq.messaging;

import com.epam.gym_app_stats_mq.api.MonthlyStatRequestInStatApp;
import com.epam.gym_app_stats_mq.exception.dlqs.dlqTriggeringException;
import com.epam.gym_app_stats_mq.stat.Stat;
import com.epam.gym_app_stats_mq.stat.StatsService;
import com.epam.gym_app_stats_mq.util.JmsMessageConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RequestMonthlyStatQueueListener {
    private final Senders mqSenders;
    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @Value("${spring.jms.monthlyStatResponse}")
    private String monthlyStatResponseQueue;

    public RequestMonthlyStatQueueListener(Senders mqSenders, StatsService statsService, ObjectMapper objectMapper) {
        this.mqSenders = mqSenders;
        this.statsService = statsService;

        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${spring.jms.requestMonthlyStat}")
    public void receivedMonthlyStatRequest(
            String monthlyStatRequest,
            @Header("gym_app_correlation_id") String correlationId
    ) throws JMSException, IOException {

        MonthlyStatRequestInStatApp monthlyStatRequestInStatApp;

        try {
            Map<String, String> map = jsonToMap(monthlyStatRequest);
            monthlyStatRequestInStatApp = MonthlyStatRequestInStatApp.fromMap(map);
            log.info("\n\nSTAT APP -> Listener -> MONTHLY STAT -> Received message with correlation ID {}: {}\n\n",
                     correlationId,
                     monthlyStatRequestInStatApp);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "STAT-APP -> STAT UPDATE Listener -> Error processing message: %s",
                    e.getMessage());
            log.error(errorMessage);
            throw new dlqTriggeringException(errorMessage);
        }

        Integer trainerId = monthlyStatRequestInStatApp.getTrainerId();
        Integer year = monthlyStatRequestInStatApp.getYear();
        Integer month = monthlyStatRequestInStatApp.getMonth();
        Map<String, Integer> monthlyStatResponse = getMonthlyStatResponse(trainerId, year, month);

        mqSenders.monthlyStatResponse(convertMapToJson(monthlyStatResponse), correlationId);

        log.info("Sent message with correlation ID {}: {}", correlationId, monthlyStatResponse);
    }

    private Map<String, Integer> getMonthlyStatResponse(Integer trainerId, Integer year, Integer month) {
        Optional<Stat> statOptional = statsService.getByTrainerIdAndYearAndMonth(trainerId, year, month);
        int result = statOptional.map(Stat::getMinutesMonthlyTotal).orElse(0);
        Map<String, Integer> response = new HashMap<>();
        response.put("minutes", result);
        return response;
    }

    private String convertMapToJson(Map<String, Integer> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error converting map to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private Map<String, String> jsonToMap(String json) throws IOException {
        // Using Jackson to convert JSON string to Map<String, String>
        return objectMapper.readValue(json, objectMapper.getTypeFactory()
                                                        .constructMapType(Map.class, String.class, String.class));
    }
}
