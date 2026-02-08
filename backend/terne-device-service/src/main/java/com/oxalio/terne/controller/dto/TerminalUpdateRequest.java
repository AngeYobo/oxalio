package com.oxalio.terne.controller.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalUpdateRequest {

  private UUID tenantId;
  private UUID posId;

  @Size(max = 50)
  private String mdmProvider;

  @Size(max = 100)
  private String mdmDeviceId;

  @Size(max = 32)
  private String appVersion;

  private List<String> tags;
}
