package com.aivya.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

/**
 * Answers every API error as a small JSON body resolved in-place, instead of
 * letting Spring forward to /error. That forward is a second internal
 * dispatch; under the stateless JWT filter chain it re-evaluates
 * authorization on a request the filter never re-signs, which turned every
 * 404/400 thrown from a service into a misleading 403. Resolving the error
 * here keeps the real status code and gives the Android app a stable shape
 * to show as a calm, specific message instead of a generic failure.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException exception) {

        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(body(
                status.value(),
                exception.getReason() != null ? exception.getReason() : status.getReasonPhrase()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                body(HttpStatus.BAD_REQUEST.value(), message));
    }

    private Map<String, Object> body(int status, String message) {
        return Map.of(
                "status", status,
                "message", message,
                "timestamp", Instant.now().toString()
        );
    }
}
