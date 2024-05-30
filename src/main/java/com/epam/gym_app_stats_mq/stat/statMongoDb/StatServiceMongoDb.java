package com.epam.gym_app_stats_mq.stat.statMongoDb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StatServiceMongoDb {
    private final StatRepoMongoDb repoMongoDb;

    @Autowired
    public StatServiceMongoDb(StatRepoMongoDb repoMongoDb) {this.repoMongoDb = repoMongoDb;}

    public void letsMongo(
            String username,
            String lastName,
            String firstName,
            Boolean status,
            Integer year,
            Integer month,
            Integer minutes,
            boolean actionTypeIsAdd
    ) {
        log.info("\n\n** MONGO ** >  LETSMONGO >> update username: {}\n\n", username);
        log.info("\n\n ------ ** MONGO ** > letsMongo > repoMongoDb.findByUserName(username) \n\n");
        StatModelMongoDb trainer = repoMongoDb.findByUserName(username);
        log.info("\n\n ** MONGO ** > trainer (StatModelMongoDb): {}\n\n", trainer);
        log.info("\n\n ++++++ ** MONGO ** > letsMongo > repoMongoDb.findByUserName(username) \n\n");

        if (trainer == null) {
            log.info("\n\n** MONGO ** >SERV> no such username, create new document \n\n");
            trainer = new StatModelMongoDb();
            trainer.setUserName(username);
            trainer.setFirstName(firstName);
            trainer.setLastName(lastName);
            trainer.setStatus(status);

            List<YearSummaryMongoDb> yearSummaryList = getYearSummaryListMongoDbs(year, month, minutes);
            trainer.setTrainingSummary(yearSummaryList);
            log.info("\n\n ------ ** MONGO ** > letsMongo> year found: repoMongoDb.save(trainer)\n\n");
            log.info("\n\n** MONGO ** > trainer (StatModelMongoDb): {}\n\n", trainer);
            repoMongoDb.save(trainer);
            log.info("\n\n +++++ ** MONGO ** > letsMongo> year found: repoMongoDb.save(trainer)\n\n");
        } else {
            log.info("\n\n** MONGO ** > SERV> document found with username: {}\n\n", username);
            List<YearSummaryMongoDb> trainingSummary = trainer.getTrainingSummary();

            boolean yearFound = false;
            for (YearSummaryMongoDb yearSummary : trainingSummary) {
                if (yearSummary.getYear().equals(year.toString())) {
                    log.info("\n\n** MONGO ** > SERV> year found for username: {}\n\n", username);
                    yearFound = true;
                    List<MonthlySummaryMongoDb> months = yearSummary.getMonths();
                    boolean monthFound = false;
                    for (MonthlySummaryMongoDb monthSummary : months) {
                        if (monthSummary.getMonth().equals(month.toString())) {
                            monthFound = true;
                            log.info("\n\n** MONGO ** > SERV> year and month found for username: {}\n\n", username);
                            int updatedDuration;
                            int currentDuration = monthSummary.getTrainingSummaryDuration();
                            if (actionTypeIsAdd) {
                                updatedDuration = currentDuration + minutes;
                            } else {
                                updatedDuration = currentDuration - minutes;
                            }
                            monthSummary.setTrainingSummaryDuration(updatedDuration);
                            break;
                        }
                    }
                    if (!monthFound) {
                        log.info("\n\n** MONGO ** > SERV> year found, month NOT found for username: {}\n\n",
                                 username);
                        if (actionTypeIsAdd) {
                            months.add(new MonthlySummaryMongoDb(month.toString(), minutes));
                        } else {
                            log.error("\n\n** MONGO ** > SERV>inconsistent: no such month, action type is DELETE");
                            months.add(new MonthlySummaryMongoDb(month.toString(), 0));
                        }
                    }
                    break;
                }
            }

            if (!yearFound) {
                log.info("\n\n** MONGO ** > SERV> year NOT found for username: {}\n\n", username);
                YearSummaryMongoDb yearSummary = getYearSummaryMongoDb(year, month, minutes);
                trainingSummary.add(yearSummary);
            }

            trainer.setTrainingSummary(trainingSummary);
            trainer.setStatus(status);
            log.info("\n\n ------ ** MONGO ** > letsMongo> year found: repoMongoDb.save(trainer)\n\n");
            log.info("\n\n** MONGO ** > trainer (StatModelMongoDb): {}\n\n", trainer);
            repoMongoDb.save(trainer);
            log.info("\n\n ++++++ ** MONGO ** > letsMongo> year found: repoMongoDb.save(trainer)\n\n");

            StatModelMongoDb savedTrainer = repoMongoDb.findByUserName(username);
            log.info("\n\n** MONGO ** > savedTrainer (StatModelMongoDb): {}\n\n", savedTrainer);

        }
    }

    private static YearSummaryMongoDb getYearSummaryMongoDb(Integer year, Integer month, Integer minutes) {
        MonthlySummaryMongoDb monthlySummary = new MonthlySummaryMongoDb(month.toString(), minutes);
        List<MonthlySummaryMongoDb> monthlySummaryList = List.of(monthlySummary);
        return new YearSummaryMongoDb(year.toString(), monthlySummaryList);
    }

    private static List<YearSummaryMongoDb> getYearSummaryListMongoDbs(Integer year, Integer month, Integer minutes) {
        MonthlySummaryMongoDb monthlySummary = new MonthlySummaryMongoDb(month.toString(), minutes);
        List<MonthlySummaryMongoDb> monthlySummaryList = List.of(monthlySummary);
        YearSummaryMongoDb yearSummary = new YearSummaryMongoDb(year.toString(), monthlySummaryList);
        return List.of(yearSummary);
    }
}

