package com.oxalio.terne.controller.dto;

import com.oxalio.terne.entity.TerminalCommandEntity;
import com.oxalio.terne.model.CommandStatus;
import com.oxalio.terne.model.CommandType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalCommandResponse {

  private UUID id;
  private UUID terminalId;

  private CommandType commandType;
  private CommandStatus status;

  private Map<String, Object> payload;

  private String requestedBy;

  private OffsetDateTime acknowledgedAt;
  private OffsetDateTime completedAt;

  private String errorCode;
  private String errorMessage;

  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public static TerminalCommandResponse from(TerminalCommandEntity e) {
    return TerminalCommandResponse.builder()
        .id(e.getId())
        .terminalId(e.getTerminalId())
        .commandType(e.getCommandType())
        .status(e.getStatus())
        .payload(e.getPayload())
        .requestedBy(e.getRequestedBy())
        .acknowledgedAt(e.getAcknowledgedAt())
        .completedAt(e.getCompletedAt())
        .errorCode(e.getErrorCode())
        .errorMessage(e.getErrorMessage())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .build();
  }
}
