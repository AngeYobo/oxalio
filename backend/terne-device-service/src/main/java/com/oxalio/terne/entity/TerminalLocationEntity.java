package com.oxalio.terne.entity;

import com.oxalio.terne.model.LocationSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "terne_terminal_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalLocationEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "terminal_id", nullable = false, columnDefinition = "uuid")
  private UUID terminalId;

  @Column(name = "captured_at", nullable = false)
  private OffsetDateTime capturedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LocationSource source;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(name = "accuracy_meters")
  private Double accuracyMeters;

  @Column(length = 50)
  private String provider;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (capturedAt == null) capturedAt = OffsetDateTime.now();
  }
}
