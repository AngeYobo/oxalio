package com.oxalio.terne.controller;

import com.oxalio.terne.controller.dto.*;
import com.oxalio.terne.entity.TerminalCommandEntity;
import com.oxalio.terne.entity.TerminalEventEntity;
import com.oxalio.terne.model.CommandStatus;
import com.oxalio.terne.model.TerminalEventType;
import com.oxalio.terne.repository.TerminalEventRepository;
import com.oxalio.terne.service.CommandService;
import com.oxalio.terne.service.TerminalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommandsController {

  private final TerminalService terminalService;
  private final CommandService commandService;
  private final TerminalEventRepository terminalEventRepository;

  @PostMapping("/api/v1/terne/terminals/{terminalId}/heartbeat")
  public ResponseEntity<Void> heartbeat(
      @PathVariable UUID terminalId,
      @Valid @RequestBody TerminalHeartbeatRequest request
  ) {
    // Ensure terminal exists and update lastSeen
    OffsetDateTime seenAt = request.getCapturedAt() != null ? request.getCapturedAt() : OffsetDateTime.now();
    terminalService.touchLastSeen(terminalId, seenAt);

    // Persist as an event for audit
    TerminalEventEntity evt = TerminalEventEntity.builder()
        .id(UUID.randomUUID())
        .terminalId(terminalId)
        .eventType(TerminalEventType.HEARTBEAT)
        .capturedAt(seenAt)
        .ipAddress(request.getIpAddress())
        .networkType(request.getNetworkType())
        .batteryPct(request.getBatteryPct())
        .charging(request.getCharging())
        .appVersion(request.getAppVersion())
        .osVersion(request.getOsVersion())
        .mdmCompliant(request.getMdmCompliant())
        .payload(request.getPayload())
        .createdAt(OffsetDateTime.now())
        .build();

    terminalEventRepository.save(evt);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/api/v1/terne/terminals/{terminalId}/commands")
  public ResponseEntity<TerminalCommandResponse> createCommand(
      @PathVariable UUID terminalId,
      @Valid @RequestBody TerminalCommandCreateRequest request
  ) {
    TerminalCommandEntity cmd = commandService.create(terminalId, request);
    return ResponseEntity.ok(TerminalCommandResponse.from(cmd));
  }

  @GetMapping("/api/v1/terne/terminals/{terminalId}/commands")
  public ResponseEntity<List<TerminalCommandResponse>> listCommands(
      @PathVariable UUID terminalId,
      @RequestParam(name = "status", required = false) CommandStatus status,
      @RequestParam(name = "limit", required = false, defaultValue = "50") int limit
  ) {
    List<TerminalCommandEntity> cmds = commandService.list(terminalId, status, limit);
    return ResponseEntity.ok(cmds.stream().map(TerminalCommandResponse::from).toList());
  }

  @PatchMapping("/api/v1/terne/commands/{commandId}")
  public ResponseEntity<TerminalCommandResponse> updateCommand(
      @PathVariable UUID commandId,
      @Valid @RequestBody TerminalCommandUpdateRequest request
  ) {
    TerminalCommandEntity updated = commandService.update(commandId, request);
    return ResponseEntity.ok(TerminalCommandResponse.from(updated));
  }
}
