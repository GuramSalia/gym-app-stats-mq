package com.epam.gym_app_stats_mq.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Senders {
    private final JmsTemplate jmsTemplate;

    @Value("${spring.jms.statUpdateResponse}")
    private String statUpdateResponseQueue;
    @Value("${spring.jms.monthlyStatResponse}")
    private String monthlyStatResponseQueue;
    @Value("${spring.jms.fullStatResponse}")
    private String fullStatResponseQueue;

    @Autowired
    public Senders(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void statUpdateResponse(String jsonUpdateResponse, String correlationId) {
        jmsTemplate.convertAndSend(statUpdateResponseQueue, jsonUpdateResponse, message -> {
            message.setStringProperty("gym_app_correlation_id", correlationId);
            return message;
        });
    }

    public void monthlyStatResponse(String jsonMonthlyStatResponse, String correlationId) {
        jmsTemplate.convertAndSend(monthlyStatResponseQueue, jsonMonthlyStatResponse, message -> {
            message.setStringProperty("gym_app_correlation_id", correlationId);
            return message;
        });
    }

    public void fullStatResponse(String jsonFullStatResponse, String correlationId) {
        jmsTemplate.convertAndSend(fullStatResponseQueue, jsonFullStatResponse, message -> {
            message.setStringProperty("gym_app_correlation_id", correlationId);
            return message;
        });
    }
}
