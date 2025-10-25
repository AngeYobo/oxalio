package com.oxalio.invoice.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class ValidationAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onInvalid(
            MethodArgumentNotValidException ex,
            ServletWebRequest req
    ) {
        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.<String, Object>of(
                        "field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")
                ))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(errorBody(HttpStatus.BAD_REQUEST, req, "VALIDATION_ERROR", errors));
    }

    private Map<String, Object> errorBody(HttpStatus status,
                                          ServletWebRequest req,
                                          String code,
                                          List<Map<String, Object>> errors) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "code", code,
                "path", Optional.ofNullable(req.getRequest()).map(r -> r.getRequestURI()).orElse(""),
                "errors", errors
        );
    }
}
