package com.epam.gym_app_stats_mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@Slf4j
@SpringBootApplication
@EnableJms
public class GymAppStatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymAppStatsApplication.class, args);
        log.info("\n\n ----------- gym-app-stat started -------------\n\n");
    }
}
