package com.oxalio.terne.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalEnrollRequest {

  @NotNull
  private UUID tenantId;

  private UUID posId;

  @NotBlank
  @Size(max = 64)
  private String serialNumber;

  @Size(max = 32)
  private String imei;

  @NotBlank
  @Size(max = 80)
  private String manufacturer;

  @NotBlank
  @Size(max = 80)
  private String model;

  @NotBlank
  @Size(max = 32)
  private String osVersion;

  @Size(max = 32)
  private String appVersion;

  @Size(max = 50)
  private String mdmProvider;

  @Size(max = 100)
  private String mdmDeviceId;

  private List<String> tags;
}
