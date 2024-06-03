package com.epam.gym_app_stats_mq;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = GymAppStatsApplication.class)
public class CucumberSpringConfiguration {
}
