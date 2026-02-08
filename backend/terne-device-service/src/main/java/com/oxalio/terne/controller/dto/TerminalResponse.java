package com.oxalio.terne.controller.dto;

import com.oxalio.terne.entity.TerminalEntity;
import com.oxalio.terne.model.TerminalStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalResponse {

  private UUID id;
  private UUID tenantId;
  private UUID posId;

  private TerminalStatus status;

  private String serialNumber;
  private String imei;
  private String manufacturer;
  private String model;

  private String osVersion;
  private String appVersion;

  private String mdmProvider;
  private String mdmDeviceId;

  private List<String> tags;

  private OffsetDateTime lastSeenAt;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public static TerminalResponse from(TerminalEntity t) {
    return TerminalResponse.builder()
        .id(t.getId())
        .tenantId(t.getTenantId())
        .posId(t.getPosId())
        .status(t.getStatus())
        .serialNumber(t.getSerialNumber())
        .imei(t.getImei())
        .manufacturer(t.getManufacturer())
        .model(t.getModel())
        .osVersion(t.getOsVersion())
        .appVersion(t.getAppVersion())
        .mdmProvider(t.getMdmProvider())
        .mdmDeviceId(t.getMdmDeviceId())
        .tags(t.getTags() == null ? null : Arrays.asList(t.getTags()))
        .lastSeenAt(t.getLastSeenAt())
        .createdAt(t.getCreatedAt())
        .updatedAt(t.getUpdatedAt())
        .build();
  }
}
