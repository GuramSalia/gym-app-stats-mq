package com.epam.gym_app_stats_mq.stat;

import com.epam.gym_app_stats_mq.api.ActionTypeInStatApp;
import com.epam.gym_app_stats_mq.api.FullStatRequestInStatApp;
import com.epam.gym_app_stats_mq.api.MonthlyStatRequestInStatApp;
import com.epam.gym_app_stats_mq.api.UpdateStatRequestInStatApp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@RestController()
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats-api/v1/trainer-full-stats")
    @Operation(summary = "Get full stats for trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer full stats retrieved successfully")
    })
    public ResponseEntity<Map<Integer, List<Map<String, Integer>>>> getTrainerFullStats(
            @Valid @RequestBody FullStatRequestInStatApp fullStatRequestInStatApp,
            @RequestHeader(name = "gym_app_correlation_id", required = false, defaultValue = "no-correlation-id") String correlationId
    ) {
        log.info("\n\nstats ms -> stats update controller -> get full stat ->  correlationId: {}\n\n", correlationId);

        Integer trainerId = fullStatRequestInStatApp.getTrainerId();
        List<Stat> fullStatsOfTrainer = statsService.getStatByTrainerId(trainerId);

        Map<Integer, List<Map<String, Integer>>> responseMap = new HashMap<>();

        for (Stat stat : fullStatsOfTrainer) {
            updateResponseMap(stat, responseMap);
        }

        return ResponseEntity.ok(responseMap);
    }

    @GetMapping("/stats-api/v1/trainer-monthly-stats")
    @Operation(summary = "Get Trainer stats (total minutes of training sessions) for a given month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer stats for a given month retrieved successfully"),
    })
    public ResponseEntity<Map<String, Integer>> getTrainerMonthlyStats(
            @Valid @RequestBody MonthlyStatRequestInStatApp monthlyStatRequestInStatApp,
            @RequestHeader(name = "gym_app_correlation_id", required = false, defaultValue = "no-correlation-id") String correlationId
    ) {
        log.info("\n\nstats ms -> stats update controller->get monthly stat ->  correlationId: {}\n\n", correlationId);

        Integer trainerId = monthlyStatRequestInStatApp.getTrainerId();
        Integer year = monthlyStatRequestInStatApp.getYear();
        Integer month = monthlyStatRequestInStatApp.getMonth();
        Map<String, Integer> response = getMonthlyStatResponse(trainerId, year, month);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stats-api/v1/trainer-stats-update")
    @Operation(summary = "update/create trainer stats for a given month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer stats updated successfully"),
            @ApiResponse(responseCode = "201", description = "Trainer stats created successfully")
    })
    public ResponseEntity<Map<String, Integer>> updateTrainerStats(
            @Valid @RequestBody UpdateStatRequestInStatApp updateStatRequestInStatApp,
            @RequestHeader(name = "gym_app_correlation_id", required = false, defaultValue = "no-correlation-id") String correlationId
    ) {

        log.info("\n\nstats ms -> stats update controller -> update stat ->  correlationId: {}\n\n", correlationId);

        Optional<Stat> statOptional = getStatOptional(updateStatRequestInStatApp);

        boolean actionTypeIsAdd = updateStatRequestInStatApp.getActionTypeInStatApp() == ActionTypeInStatApp.ADD;
        Integer minutes = updateStatRequestInStatApp.getDuration();
        Integer trainerId = updateStatRequestInStatApp.getTrainerId();
        Integer year = updateStatRequestInStatApp.getYear();
        Integer month = updateStatRequestInStatApp.getMonth();
        log.info("updating stats");

        if (statOptional.isPresent()) {
            Stat stat = statOptional.get();
            Integer currentMinutes = stat.getMinutesMonthlyTotal();
            int newMinutes;
            if (actionTypeIsAdd) {
                newMinutes = currentMinutes + minutes;
            } else {
                newMinutes = currentMinutes - minutes;
            }

            stat.setMinutesMonthlyTotal(newMinutes);
            statsService.updateStat(stat);
            Map<String, Integer> response = getMonthlyStatResponse(trainerId, year, month);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        Stat stat = getStat(updateStatRequestInStatApp);
        statsService.createStat(stat);
        Map<String, Integer> response = getMonthlyStatResponse(trainerId, year, month);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void logRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("Request Header: {} = {}", headerName, headerValue);
        }
    }

    private Map<String, Integer> getMonthlyStatResponse(Integer trainerId, Integer year, Integer month) {
        Optional<Stat> statOptional = statsService.getByTrainerIdAndYearAndMonth(trainerId, year, month);
        int result = statOptional.map(Stat::getMinutesMonthlyTotal).orElse(0);
        Map<String, Integer> response = new HashMap<>();
        response.put("minutes", result);
        return response;
    }

    private void updateResponseMap(Stat stat, Map<Integer, List<Map<String, Integer>>> responseMap) {
        int year = stat.getYear();
        Integer monthInt = stat.getMonth();
        String monthString = convertMonthIntToMonthString(monthInt);
        int minutes = stat.getMinutesMonthlyTotal();
        responseMap.putIfAbsent(year, new ArrayList<>());
        List<Map<String, Integer>> yearStats = responseMap.get(year);
        Map<String, Integer> monthMinutesPair = new HashMap<>();
        monthMinutesPair.put(monthString, minutes);
        log.info("\n\n -------year: {}, month: {}, minutes {} ---- in updateResponseMap----- \n\n ",
                 year, monthString, minutes);
        yearStats.add(monthMinutesPair);
    }

    private String convertMonthIntToMonthString(int monthInt) {
        LocalDate localDate = LocalDate.of(2000, monthInt, 1);
        return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private Optional<Stat> getStatOptional(UpdateStatRequestInStatApp updateStatRequestInStatApp) {
        Integer trainerId = updateStatRequestInStatApp.getTrainerId();
        Integer year = updateStatRequestInStatApp.getYear();
        Integer month = updateStatRequestInStatApp.getMonth();
        return statsService.getByTrainerIdAndYearAndMonth(trainerId, year, month);
    }

    private Stat getStat(UpdateStatRequestInStatApp updateStatRequestInStatApp) {
        Stat stat = new Stat();
        Integer trainerId = updateStatRequestInStatApp.getTrainerId();
        Integer year = updateStatRequestInStatApp.getYear();
        Integer month = updateStatRequestInStatApp.getMonth();
        stat.setTrainerId(trainerId);
        stat.setYear(year);
        stat.setMonth(month);
        stat.setMinutesMonthlyTotal(updateStatRequestInStatApp.getDuration());
        return stat;
    }
}
