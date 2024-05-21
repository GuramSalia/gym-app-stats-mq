package com.epam.gym_app_stats_mq.stat;

import com.epam.gym_app_stats_mq.api.*;
import com.epam.gym_app_stats_mq.exception.InvalidTokenException;
import com.epam.gym_app_stats_mq.proxy.TokenValidationProxy;
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
    private final TokenValidationProxy proxy;

    @Autowired
    public StatsController(StatsService statsService, TokenValidationProxy proxy) {
        this.statsService = statsService;
        this.proxy = proxy;
    }

    @GetMapping("/stats-api/v1/trainer-full-stats")
    @Operation(summary = "Get full stats for trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer full stats retrieved successfully")
    })
    public ResponseEntity<Map<Integer, List<Map<String, Integer>>>> getTrainerFullStats(
            @Valid @RequestBody FullStatRequest fullStatRequest,
            @RequestHeader(name = "gym-app-correlation-id", required = false, defaultValue = "no-correlation-id") String correlationId
    ) {
        log.info("\n\nstats ms -> stats update controller -> get full stat ->  correlationId: {}\n\n", correlationId);

        String jwtToken = fullStatRequest.getToken();
        validateJwtToken(jwtToken);

        Integer trainerId = fullStatRequest.getTrainerId();
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
            @Valid @RequestBody MonthlyStatRequest monthlyStatRequest,
            @RequestHeader(name = "gym-app-correlation-id", required = false, defaultValue = "no-correlation-id") String correlationId
    ) {
        log.info("\n\nstats ms -> stats update controller->get monthly stat ->  correlationId: {}\n\n", correlationId);

        String jwtToken = monthlyStatRequest.getToken();
        validateJwtToken(jwtToken);

        Integer trainerId = monthlyStatRequest.getTrainerId();
        Integer year = monthlyStatRequest.getYear();
        Integer month = monthlyStatRequest.getMonth();
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
            @Valid @RequestBody UpdateStatRequest updateStatRequest,
            @RequestHeader(name = "gym-app-correlation-id", required = false, defaultValue = "no-correlation-id") String correlationId
    ) {


        log.info("\n\nstats ms -> stats update controller -> update stat ->  correlationId: {}\n\n", correlationId);

        String jwtToken = updateStatRequest.getToken();
        validateJwtToken(jwtToken);

        Optional<Stat> statOptional = getStatOptional(updateStatRequest);

        boolean actionTypeIsAdd = updateStatRequest.getActionType() == ActionType.ADD;
        Integer minutes = updateStatRequest.getDuration();
        Integer trainerId = updateStatRequest.getTrainerId();
        Integer year = updateStatRequest.getYear();
        Integer month = updateStatRequest.getMonth();
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

        Stat stat = getStat(updateStatRequest);
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

    private Optional<Stat> getStatOptional(UpdateStatRequest updateStatRequest) {
        Integer trainerId = updateStatRequest.getTrainerId();
        Integer year = updateStatRequest.getYear();
        Integer month = updateStatRequest.getMonth();
        return statsService.getByTrainerIdAndYearAndMonth(trainerId, year, month);
    }

    private Stat getStat(UpdateStatRequest updateStatRequest) {
        Stat stat = new Stat();
        Integer trainerId = updateStatRequest.getTrainerId();
        Integer year = updateStatRequest.getYear();
        Integer month = updateStatRequest.getMonth();
        stat.setTrainerId(trainerId);
        stat.setYear(year);
        stat.setMonth(month);
        stat.setMinutesMonthlyTotal(updateStatRequest.getDuration());
        return stat;
    }

    private void validateJwtToken(String jwtToken) {
        log.info("\n\nstats ms -> stats update controller -> validate jwt token -> jwtToken: {}\n\n", jwtToken);
        TokenValidationRequest tokenValidationRequest = new TokenValidationRequest();
        tokenValidationRequest.setToken(jwtToken);
        log.info("\n\n tokenValidationRequest: {}\n\n", tokenValidationRequest);
        ResponseEntity<TokenValidationResponse> responseEntity = proxy.validateToken(tokenValidationRequest, "no-correlation-id");
        log.info("\n\nstats ms -> stats update controller -> validate jwt token -> responseEntity: {}\n\n", responseEntity);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new InvalidTokenException("could not validate the jwt token");
        }

    }
}
