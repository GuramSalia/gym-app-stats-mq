package com.epam.gym_app_stats_mq.messaging;

import com.epam.gym_app_stats_mq.api.ActionTypeInStatApp;
import com.epam.gym_app_stats_mq.api.UpdateStatRequestInStatApp;
import com.epam.gym_app_stats_mq.stat.Stat;
import com.epam.gym_app_stats_mq.stat.StatsService;
import com.epam.gym_app_stats_mq.util.JmsMessageConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class RequestStatUpdateQueueListener {
    private final Senders mqSenders;
    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @Value("${spring.jms.statUpdateResponse}")
    private String statUpdateResponseQueue;

    @Autowired
    public RequestStatUpdateQueueListener(Senders mqSenders, StatsService statsService, ObjectMapper objectMapper) {
        this.mqSenders = mqSenders;
        this.statsService = statsService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${spring.jms.requestStatUpdate}")
    public void receivedStatUpdateRequest(
            String updateStatRequest,
            @Header("gym_app_correlation_id") String correlationId
    ) throws JMSException, IOException {
        Map<String, String> map = jsonToMap(updateStatRequest);
        UpdateStatRequestInStatApp updateStatRequestInStatApp = UpdateStatRequestInStatApp.fromMap(map);
        log.info("\n\nSTAT APP -> Listener -> STAT UPDATE -> Received message with correlation ID {}: {}\n\n",
                 correlationId, updateStatRequestInStatApp);

        Optional<Stat> statOptional = getStatOptional(updateStatRequestInStatApp);

        boolean actionTypeIsAdd = updateStatRequestInStatApp.getActionTypeInStatApp() == ActionTypeInStatApp.ADD;
        Integer minutes = updateStatRequestInStatApp.getDuration();
        Integer trainerId = updateStatRequestInStatApp.getTrainerId();
        Integer year = updateStatRequestInStatApp.getYear();
        Integer month = updateStatRequestInStatApp.getMonth();
        log.info("updating stats");

        Map<String, Integer> updateResponse;

        if (statOptional.isPresent()) {
            Stat stat = statOptional.get();
            Integer currentMinutes = stat.getMinutesMonthlyTotal();
            int newMinutes;
            if (actionTypeIsAdd) {
                newMinutes = currentMinutes + minutes;
            } else {
                newMinutes = currentMinutes - minutes;
            }

            stat.setMinutesMonthlyTotal(newMinutes);
            statsService.updateStat(stat);
            updateResponse = getMonthlyStatResponse(trainerId, year, month);

        } else {
            Stat stat = getStat(updateStatRequestInStatApp);
            statsService.createStat(stat);
            updateResponse = getMonthlyStatResponse(trainerId, year, month);
        }

        mqSenders.statUpdateResponse(convertMapToJson(updateResponse), correlationId);

        log.info("Sent message with correlation ID {}: {}", correlationId, updateResponse);

    }

    private Optional<Stat> getStatOptional(UpdateStatRequestInStatApp updateStatRequestInStatApp) {
        Integer trainerId = updateStatRequestInStatApp.getTrainerId();
        Integer year = updateStatRequestInStatApp.getYear();
        Integer month = updateStatRequestInStatApp.getMonth();
        return statsService.getByTrainerIdAndYearAndMonth(trainerId, year, month);
    }

    private Stat getStat(UpdateStatRequestInStatApp updateStatRequestInStatApp) {
        Stat stat = new Stat();
        Integer trainerId = updateStatRequestInStatApp.getTrainerId();
        Integer year = updateStatRequestInStatApp.getYear();
        Integer month = updateStatRequestInStatApp.getMonth();
        stat.setTrainerId(trainerId);
        stat.setYear(year);
        stat.setMonth(month);
        stat.setMinutesMonthlyTotal(updateStatRequestInStatApp.getDuration());
        return stat;
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
            return "{}"; // Return an empty JSON object in case of error
        }
    }

    private Map<String, String> jsonToMap(String json) throws IOException {
        // Using Jackson to convert JSON string to Map<String, String>
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
    }
}
