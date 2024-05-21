package com.epam.gym_app_stats_mq.stat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StatsService {
    @Autowired
    private StatsRepo statsRepo;

    public void updateStat(Stat stat) {
        log.info("Updating stats");
        statsRepo.save(stat);
    }

    public void createStat(Stat stat) {
        log.info("Creating stats");
        statsRepo.save(stat);
    }

    public void deleteStat(Stat stat) {
        log.info("Deleting stats");
        statsRepo.delete(stat);
    }

    public List<Stat> getStatByTrainerId(Integer trainerId) {
        log.info("Getting stats by trainer id");
        return statsRepo.findByTrainerId(trainerId);
    }


    public Optional<Stat> getByTrainerIdAndYearAndMonth(Integer trainerId, Integer year, Integer month) {
        log.info("Getting stats by trainer id and year");
        return statsRepo.findByTrainerIdAndYearAndMonth(trainerId, year, month);
    }

}
