package com.oxalio.terne.controller;

import com.oxalio.terne.controller.dto.*;
import com.oxalio.terne.entity.TerminalEntity;
import com.oxalio.terne.model.TerminalStatus;
import com.oxalio.terne.repository.TerminalRepository;
import com.oxalio.terne.service.TerminalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terne/terminals")
@RequiredArgsConstructor
public class TerminalsController {

  private final TerminalService terminalService;
  private final TerminalRepository terminalRepository;

  @PostMapping
  public ResponseEntity<TerminalResponse> enroll(@Valid @RequestBody TerminalEnrollRequest request) {
    TerminalEntity t = terminalService.enroll(request);
    return ResponseEntity.ok(TerminalResponse.from(t));
  }

  @GetMapping
  public ResponseEntity<List<TerminalResponse>> list(
      @RequestParam(name = "tenantId", required = false) UUID tenantId,
      @RequestParam(name = "posId", required = false) UUID posId,
      @RequestParam(name = "status", required = false) TerminalStatus status
  ) {
    // Minimal list (can be replaced later by Specifications)
    List<TerminalEntity> terminals = terminalRepository.findAll().stream()
        .filter(t -> tenantId == null || tenantId.equals(t.getTenantId()))
        .filter(t -> posId == null || (t.getPosId() != null && posId.equals(t.getPosId())))
        .filter(t -> status == null || status.equals(t.getStatus()))
        .toList();

    return ResponseEntity.ok(terminals.stream().map(TerminalResponse::from).toList());
  }

  @GetMapping("/{terminalId}")
  public ResponseEntity<TerminalResponse> get(@PathVariable UUID terminalId) {
    return ResponseEntity.ok(TerminalResponse.from(terminalService.get(terminalId)));
  }

  @PatchMapping("/{terminalId}")
  public ResponseEntity<TerminalResponse> update(
      @PathVariable UUID terminalId,
      @Valid @RequestBody TerminalUpdateRequest request
  ) {
    TerminalEntity t = terminalService.update(terminalId, request);
    return ResponseEntity.ok(TerminalResponse.from(t));
  }

  @PostMapping("/{terminalId}/activate")
  public ResponseEntity<TerminalResponse> activate(@PathVariable UUID terminalId) {
    return ResponseEntity.ok(TerminalResponse.from(terminalService.activate(terminalId)));
  }

  @PostMapping("/{terminalId}/suspend")
  public ResponseEntity<TerminalResponse> suspend(@PathVariable UUID terminalId) {
    return ResponseEntity.ok(TerminalResponse.from(terminalService.suspend(terminalId)));
  }
}
