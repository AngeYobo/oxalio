package com.oxalio.terne.entity;

import com.oxalio.terne.model.NetworkType;
import com.oxalio.terne.model.TerminalEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "terne_terminal_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalEventEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "terminal_id", nullable = false, columnDefinition = "uuid")
  private UUID terminalId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private TerminalEventType eventType;

  @Column(name = "captured_at", nullable = false)
  private OffsetDateTime capturedAt;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Enumerated(EnumType.STRING)
  @Column(name = "network_type")
  private NetworkType networkType;

  @Column(name = "battery_pct")
  private Integer batteryPct;

  private Boolean charging;

  @Column(name = "app_version", length = 32)
  private String appVersion;

  @Column(name = "os_version", length = 32)
  private String osVersion;

  @Column(name = "mdm_compliant")
  private Boolean mdmCompliant;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> payload;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (capturedAt == null) capturedAt = OffsetDateTime.now();
  }
}
