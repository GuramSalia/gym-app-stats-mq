package com.epam.gym_app_stats_mq.util;

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JmsMessageConverter {
    private JmsMessageConverter() {
    }

    public static Map<String, String> convertMapMessageToMap(MapMessage message) throws JMSException {
        Map<String, String> map = new HashMap<>();
        Enumeration<?> enumeration = message.getMapNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            map.put(key, message.getString(key));
        }
        return map;
    }
}
