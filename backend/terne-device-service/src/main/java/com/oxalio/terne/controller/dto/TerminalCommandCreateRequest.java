package com.oxalio.terne.controller.dto;

import com.oxalio.terne.model.CommandType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalCommandCreateRequest {

  @NotNull
  private CommandType type;

  private Map<String, Object> payload;

  @Size(max = 120)
  private String requestedBy;
}
