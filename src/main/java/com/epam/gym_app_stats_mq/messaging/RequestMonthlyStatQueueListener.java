package com.epam.gym_app_stats_mq.messaging;

import com.epam.gym_app_stats_mq.api.MonthlyStatRequestInStatApp;
import com.epam.gym_app_stats_mq.stat.Stat;
import com.epam.gym_app_stats_mq.stat.StatsService;
import com.epam.gym_app_stats_mq.util.JmsMessageConverter;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RequestMonthlyStatQueueListener {
    private final Senders mqSenders;
    private final StatsService statsService;

    @Value("${spring.jms.monthlyStatResponse}")
    private String monthlyStatResponseQueue;

    public RequestMonthlyStatQueueListener(Senders mqSenders, StatsService statsService) {
        this.mqSenders = mqSenders;
        this.statsService = statsService;

    }

    @JmsListener(destination = "${spring.jms.requestMonthlyStat}")
    public void receivedMonthlyStatRequest(
            MapMessage message,
            @Header("gym_app_correlation_id") String correlationId) throws JMSException {
        Map<String, String> map = JmsMessageConverter.convertMapMessageToMap(message);
        MonthlyStatRequestInStatApp monthlyStatRequestInStatApp = MonthlyStatRequestInStatApp.fromMap(map);
        log.info("\n\nSTAT APP -> Listener -> MONTHLY STAT -> Received message with correlation ID {}: {}\n\n",
                 correlationId,
                 monthlyStatRequestInStatApp);

        Integer trainerId = monthlyStatRequestInStatApp.getTrainerId();
        Integer year = monthlyStatRequestInStatApp.getYear();
        Integer month = monthlyStatRequestInStatApp.getMonth();
        Map<String, Integer> monthlyStatResponse = getMonthlyStatResponse(trainerId, year, month);

        mqSenders.monthlyStatResponse(monthlyStatResponse, correlationId);

        log.info("Sent message with correlation ID {}: {}", correlationId, monthlyStatResponse);
    }

    private Map<String, Integer> getMonthlyStatResponse(Integer trainerId, Integer year, Integer month) {
        Optional<Stat> statOptional = statsService.getByTrainerIdAndYearAndMonth(trainerId, year, month);
        int result = statOptional.map(Stat::getMinutesMonthlyTotal).orElse(0);
        Map<String, Integer> response = new HashMap<>();
        response.put("minutes", result);
        return response;
    }
}
