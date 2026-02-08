package com.oxalio.terne.controller.dto;

import com.oxalio.terne.model.LocationSource;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalLocationIngestRequest {

  @NotNull
  private OffsetDateTime capturedAt;

  @NotNull
  private LocationSource source;

  @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
  private double latitude;

  @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
  private double longitude;

  @PositiveOrZero
  private Double accuracyMeters;

  @Size(max = 50)
  private String provider;
}
