package com.oxalio.terne.controller.dto;

import com.oxalio.terne.model.NetworkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalHeartbeatRequest {

  private OffsetDateTime capturedAt;

  private String ipAddress;

  private NetworkType networkType;

  @Min(0) @Max(100)
  private Integer batteryPct;

  private Boolean charging;

  private String appVersion;
  private String osVersion;

  private Boolean mdmCompliant;

  private Map<String, Object> payload;
}
