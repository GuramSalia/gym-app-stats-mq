package com.epam.gym_app_stats_mq.stat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatsRepo extends JpaRepository<Stat, Integer> {

    List<Stat> findByTrainerId(Integer trainerId);

    Optional<Stat> findByTrainerIdAndYearAndMonth(Integer trainerId, Integer year, Integer month);
}
