package com.oxalio.terne.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

  private OffsetDateTime timestamp;
  private Integer status;
  private String error;
  private String message;
  private String path;

  // Traçabilité / audit
  private String errorId;        // UUID généré serveur, toujours renvoyé
  private String correlationId;  // X-Correlation-Id (si présent)

  // Détails de validation
  private List<FieldError> validationErrors;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FieldError {
    private String field;
    private String message;
  }
}
