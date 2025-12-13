package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyAttempts(TooManyLoginAttemptsException e){

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                Map.of(
                        "timestamps", LocalDateTime.now(),
                        "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                        "error", "Too many attempts",
                        "message", e.getMessage()
                )
        );
    }
}
