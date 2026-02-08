package com.oxalio.terne.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@org.springframework.web.bind.annotation.RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String HDR_CORR = "X-Correlation-Id";
  private static final String HDR_ERROR_ID = "X-Error-Id";

  @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null, ex);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req, null, ex);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Validation error", req, null, ex);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ApiErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> ApiErrorResponse.FieldError.builder()
            .field(fe.getField())
            .message(fe.getDefaultMessage())
            .build())
        .toList();
    return build(HttpStatus.BAD_REQUEST, "Validation error", req, errors, ex);
  }

  // JSON invalide / body illisible
  @org.springframework.web.bind.annotation.ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleJsonNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Invalid JSON payload", req, null, ex);
  }

  // Paramètre requis manquant
  @org.springframework.web.bind.annotation.ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName(), req, null, ex);
  }

  // UUID/Enum invalide dans query/path
  @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName(), req, null, ex);
  }

  // Mauvaise méthode HTTP
  @org.springframework.web.bind.annotation.ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", req, null, ex);
  }

  // Content-Type non supporté
  @org.springframework.web.bind.annotation.ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
    return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", req, null, ex);
  }

  // URL inexistante (ressources statiques / endpoints non mappés)
  @org.springframework.web.bind.annotation.ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "Not found", req, null, ex);
  }

  // Catch-all
  @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, null, ex);
  }

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status,
      String message,
      HttpServletRequest req,
      List<ApiErrorResponse.FieldError> validationErrors,
      Exception ex
  ) {
    String correlationId = readCorrelationId(req);
    String errorId = UUID.randomUUID().toString();

    // Logs “audit”
    if (status.is5xxServerError()) {
      log.error("errorId={} corrId={} {} {} -> {}", errorId, correlationId, req.getMethod(), req.getRequestURI(), status.value(), ex);
    } else {
      log.warn("errorId={} corrId={} {} {} -> {} : {}", errorId, correlationId, req.getMethod(), req.getRequestURI(), status.value(), message);
      log.debug("errorId={} corrId={} exception", errorId, correlationId, ex);
    }

    ApiErrorResponse body = ApiErrorResponse.builder()
        .timestamp(OffsetDateTime.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(message)
        .path(req.getRequestURI())
        .errorId(errorId)
        .correlationId(correlationId)
        .validationErrors(validationErrors)
        .build();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HDR_ERROR_ID, errorId);
    if (correlationId != null && !correlationId.isBlank()) {
      headers.add(HDR_CORR, correlationId); // echo
    }

    return new ResponseEntity<>(body, headers, status);
  }

  private String readCorrelationId(HttpServletRequest req) {
    String v = req.getHeader(HDR_CORR);
    if (v == null || v.isBlank()) return null;
    return v.length() > 128 ? v.substring(0, 128) : v;
  }
}
