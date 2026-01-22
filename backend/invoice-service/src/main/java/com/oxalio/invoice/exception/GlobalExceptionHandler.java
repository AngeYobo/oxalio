package com.oxalio.invoice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Active l’inclusion de détails (root cause + stacktrace) via config.
     * Mets app.errors.show-details=true en profil mock/dev.
     */
    @Value("${app.errors.show-details:false}")
    private boolean showDetails;

    // ---------- 400: Validation @Valid (DTO corps JSON) ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            fieldErrors.put(field, err.getDefaultMessage());
        });
        log.warn("Validation failed on {}: {}", path(request), fieldErrors);

        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Les données de la requête ne sont pas valides", request, fieldErrors, ex);
    }

    // ---------- 400: Violations Jakarta au niveau paramètres (@RequestParam, etc.) ----------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            violations.put(v.getPropertyPath().toString(), v.getMessage());
        }
        log.warn("Constraint violation on {}: {}", path(request), violations);

        return build(HttpStatus.BAD_REQUEST, "Constraint Violation",
                "Paramètres invalides", request, violations, ex);
    }

    // ---------- 400: JSON invalide / body illisible ----------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Unreadable JSON on {}: {}", path(request), root(ex));
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON",
                "Le corps de la requête est invalide ou illisible", request, null, ex);
    }

    // ---------- 400: Mauvais type (p.ex. id=abc au lieu d’un long) ----------
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch on {}: {}", path(request), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Type Mismatch",
                "Le type d’un paramètre est invalide", request, Map.of(ex.getName(), "Type attendu: " +
                        (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu")), ex);
    }

    // ---------- 400: Paramètre manquant ----------
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing param on {}: {}", path(request), ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "Missing Parameter",
                "Paramètre requis manquant", request, Map.of(ex.getParameterName(), "requis"), ex);
    }

    // ---------- 400: BindException (path params / form) ----------
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        log.warn("Bind error on {}: {}", path(request), fieldErrors);

        return build(HttpStatus.BAD_REQUEST, "Binding Failed",
                "Les paramètres fournis sont invalides", request, fieldErrors, ex);
    }

    // ---------- 404: Ressource métier ----------
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFound(InvoiceNotFoundException ex, WebRequest request) {
        log.warn("Not found on {}: {}", path(request), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null, ex);
    }

    // ---------- 405: Méthode HTTP non supportée ----------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Method not supported on {}: {}", path(request), ex.getMethod());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "Méthode HTTP non supportée", request, null, ex);
    }

    // ---------- 409: Contrainte DB (NOT NULL, FK, unique, etc.) ----------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation on {}: {}", path(request), root(ex));
        return build(HttpStatus.CONFLICT, "Data Integrity Violation",
                "Contrainte de base de données violée", request, null, ex);
    }

    // ---------- 400: Mauvaise requête générique ----------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument on {}: {}", path(request), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, null, ex);
    }

    // ---------- 404: Endpoint/ressource inexistante (Spring static resource / no handler) ----------
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        log.warn("No resource found on {}: {}", path(request), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found",
                "Ressource introuvable", request, null, ex);
    }


    // ---------- 500: Fallback ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled error on {}: {}", path(request), root(ex), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Une erreur inattendue s'est produite", request, null, ex);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, WebRequest request) {
        log.warn("No handler found on {}: {} {}", path(request), ex.getHttpMethod(), ex.getRequestURL());
        return build(HttpStatus.NOT_FOUND, "Not Found",
                "Endpoint introuvable", request, null, ex);
    }


    // ---------- Helpers ----------
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            WebRequest request,
            Map<String, String> validationErrors,
            Exception ex
    ) {
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path(request))
                .validationErrors(validationErrors);

        if (showDetails && ex != null) {
            builder.detail(root(ex));
            builder.trace(stack(ex));
        }
        return ResponseEntity.status(status).body(builder.build());
    }

    private String path(WebRequest req) {
        return req.getDescription(false).replace("uri=", "");
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getClass().getSimpleName() + ": " + (r.getMessage() == null ? "" : r.getMessage());
    }

    private String stack(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Data
    @Builder
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, String> validationErrors;

        // champs optionnels (profil dev/mock)
        private String detail;   // root cause
        private String trace;    // stacktrace
    }
}
