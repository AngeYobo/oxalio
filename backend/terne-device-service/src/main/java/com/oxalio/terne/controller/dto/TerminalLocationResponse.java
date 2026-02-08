package com.oxalio.terne.controller.dto;

import com.oxalio.terne.entity.TerminalLocationEntity;
import com.oxalio.terne.model.LocationSource;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalLocationResponse {

  private UUID id;
  private UUID terminalId;

  private OffsetDateTime capturedAt;
  private LocationSource source;

  private double latitude;
  private double longitude;

  private Double accuracyMeters;
  private String provider;

  private OffsetDateTime createdAt;

  public static TerminalLocationResponse from(TerminalLocationEntity e) {
    return TerminalLocationResponse.builder()
        .id(e.getId())
        .terminalId(e.getTerminalId())
        .capturedAt(e.getCapturedAt())
        .source(e.getSource())
        .latitude(e.getLatitude())
        .longitude(e.getLongitude())
        .accuracyMeters(e.getAccuracyMeters())
        .provider(e.getProvider())
        .createdAt(e.getCreatedAt())
        .build();
  }
}
