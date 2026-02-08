package com.oxalio.terne.controller.dto;

import com.oxalio.terne.model.CommandStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalCommandUpdateRequest {

  @NotNull
  private CommandStatus status;

  private OffsetDateTime acknowledgedAt;
  private OffsetDateTime completedAt;

  @Size(max = 80)
  private String errorCode;

  @Size(max = 1000)
  private String errorMessage;
}
