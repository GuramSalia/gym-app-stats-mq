package com.epam.gym_app_stats_mq.exception;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ErrorDetails> handleInvalidBearerTokenException(
            InvalidTokenException ex,
            WebRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request.getDescription(false)
        );

        log.info("\n\n>> from error handler > handleInvalidBearerTokenException: "
                         + request.getDescription(false) + "\n");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<ErrorDetails> handleNullPointerException(
            NullPointerException ex,
            WebRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST,
                "A null pointer exception occurred. Please check your request and try again.",
                request.getDescription(false)
        );

        log.error("NullPointerException occurred: {} - Request Details: {}",
                  ex.getMessage(),
                  request.getDescription(false),
                  ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public final ResponseEntity<ErrorDetails> handleAllException(
            Exception ex, WebRequest request
    ) {
        log.info("\n\n>> from error handler > handleAllException \n");
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }
}
