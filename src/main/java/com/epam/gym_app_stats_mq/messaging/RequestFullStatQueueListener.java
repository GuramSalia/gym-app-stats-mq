package com.epam.gym_app_stats_mq.messaging;

import com.epam.gym_app_stats_mq.api.FullStatRequestInStatApp;
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
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Component
@Slf4j
public class RequestFullStatQueueListener {

    private final Senders mqSenders;
    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @Value("${spring.jms.fullStatResponse}")
    private String fullStatResponseQueue;

    @Autowired
    public RequestFullStatQueueListener(Senders mqSenders, StatsService statsService, ObjectMapper objectMapper) {
        this.mqSenders = mqSenders;
        this.statsService = statsService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${spring.jms.requestFullStat}")
    public void receivedFullStatRequest(
            String fullStatRequest,
            @Header("gym_app_correlation_id") String correlationId) throws JMSException, IOException {
        Map<String, String> map = jsonToMap(fullStatRequest);
        FullStatRequestInStatApp fullStatRequestInStatApp = FullStatRequestInStatApp.fromMap(map);
        log.info("\n\nSTAT APP -> Listener -> FULL STAT -> Received message with correlation ID {}: {}\n\n",
                 correlationId, fullStatRequestInStatApp);

        Integer trainerId = fullStatRequestInStatApp.getTrainerId();
        List<Stat> fullStatsOfTrainer = statsService.getStatByTrainerId(trainerId);

        Map<String, List<Map<String, Integer>>> fullStatResponse = new HashMap<>();

        for (Stat stat : fullStatsOfTrainer) {
            updateResponseMap(stat, fullStatResponse);
        }

        String jsonResponse = convertMapToJson(fullStatResponse);

        mqSenders.fullStatResponse(jsonResponse, correlationId);

        log.info("\n\nSTAT APP -> Listener -> FULL STAT ->Sent message with correlation ID {}: {}\n\n",
                 correlationId, fullStatResponse);
    }

    private void updateResponseMap(Stat stat, Map<String, List<Map<String, Integer>>> responseMap) {
        int year = stat.getYear();
        String yearKey = String.valueOf(year);
        Integer monthInt = stat.getMonth();
        String monthString = convertMonthIntToMonthString(monthInt);
        int minutes = stat.getMinutesMonthlyTotal();
        responseMap.putIfAbsent(yearKey, new ArrayList<>());
        List<Map<String, Integer>> yearStats = responseMap.get(yearKey);
        Map<String, Integer> monthMinutesPair = new HashMap<>();
        monthMinutesPair.put(monthString, minutes);
        log.info("\n\n -------year: {}, month: {}, minutes {} ---- in updateResponseMap----- \n\n ",
                 year, monthString, minutes);
        yearStats.add(monthMinutesPair);
    }

    private String convertMonthIntToMonthString(int monthInt) {
        LocalDate localDate = LocalDate.of(2000, monthInt, 1);
        return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private String convertMapToJson(Map<String, List<Map<String, Integer>>> map) {
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
