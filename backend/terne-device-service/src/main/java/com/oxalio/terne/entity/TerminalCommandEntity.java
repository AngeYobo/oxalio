package com.oxalio.terne.entity;

import com.oxalio.terne.model.CommandStatus;
import com.oxalio.terne.model.CommandType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "terne_terminal_commands")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalCommandEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "terminal_id", nullable = false, columnDefinition = "uuid")
  private UUID terminalId;

  @Enumerated(EnumType.STRING)
  @Column(name = "command_type", nullable = false)
  private CommandType commandType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CommandStatus status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> payload;

  @Column(name = "requested_by", length = 120)
  private String requestedBy;

  @Column(name = "acknowledged_at")
  private OffsetDateTime acknowledgedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "error_code", length = 80)
  private String errorCode;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

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
    if (status == null) status = CommandStatus.QUEUED;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
