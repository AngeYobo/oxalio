package com.oxalio.terne.entity;

import com.oxalio.terne.model.TerminalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "terne_terminals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
  private UUID tenantId;

  @Column(name = "pos_id", columnDefinition = "uuid")
  private UUID posId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TerminalStatus status;

  @Column(name = "serial_number", nullable = false, length = 64, updatable = false)
  private String serialNumber;

  @Column(length = 32, updatable = false)
  private String imei;

  @Column(nullable = false, length = 80)
  private String manufacturer;

  @Column(nullable = false, length = 80)
  private String model;

  @Column(name = "os_version", nullable = false, length = 32)
  private String osVersion;

  @Column(name = "app_version", length = 32)
  private String appVersion;

  @Column(name = "mdm_provider", length = 50)
  private String mdmProvider;

  @Column(name = "mdm_device_id", length = 100)
  private String mdmDeviceId;

  @Column(name = "tags", columnDefinition = "text[]")
  private String[] tags;


  @Column(name = "last_seen_at")
  private OffsetDateTime lastSeenAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
    var now = OffsetDateTime.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
    if (status == null) status = TerminalStatus.ENROLLED;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
