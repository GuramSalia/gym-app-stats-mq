package com.epam.gym_app_stats_mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableFeignClients
public class GymAppStatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymAppStatsApplication.class, args);
		log.info("\n\n ----------- app started -------------\n\n");

	}

}
