package com.epam.gym_app_stats_mq.stat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "STATS")
@Getter
@Setter
@Slf4j
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "TRAINER_ID")
    private Integer trainerId;

    @Column(name = "YEAR_")
    private Integer year;

    @Column(name = "MONTH_")
    private Integer month;

    @Column(name = "MINUTES_MONTHLY_TOTAL")
    private Integer minutesMonthlyTotal;

    public Stat() {}

    public Stat(Integer id, Integer trainerId, Integer year, Integer month, Integer minutesMonthlyTotal) {
        this.id = id;
        this.trainerId = trainerId;
        this.year = year;
        this.month = month;
        this.minutesMonthlyTotal = minutesMonthlyTotal;
    }
}
