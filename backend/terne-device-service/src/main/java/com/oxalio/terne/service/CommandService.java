package com.oxalio.terne.service;

import com.oxalio.terne.controller.dto.TerminalCommandCreateRequest;
import com.oxalio.terne.controller.dto.TerminalCommandUpdateRequest;
import com.oxalio.terne.entity.TerminalCommandEntity;
import com.oxalio.terne.exception.NotFoundException;
import com.oxalio.terne.model.CommandStatus;
import com.oxalio.terne.repository.TerminalCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommandService {

  private final TerminalService terminalService;
  private final TerminalCommandRepository terminalCommandRepository;

  @Transactional
  public TerminalCommandEntity create(UUID terminalId, TerminalCommandCreateRequest req) {
    terminalService.get(terminalId);

    OffsetDateTime now = OffsetDateTime.now();

    TerminalCommandEntity cmd = TerminalCommandEntity.builder()
        .id(UUID.randomUUID())
        .terminalId(terminalId)
        .commandType(req.getType())
        .status(CommandStatus.QUEUED) // ✅ aligned with SQL CHECK
        .payload(req.getPayload())
        .requestedBy(req.getRequestedBy())
        .acknowledgedAt(null)
        .completedAt(null)
        .errorCode(null)
        .errorMessage(null)
        .createdAt(now)
        .updatedAt(now)
        .build();

    return terminalCommandRepository.save(cmd);
  }

  @Transactional(readOnly = true)
  public List<TerminalCommandEntity> list(UUID terminalId, CommandStatus status, int limit) {
    terminalService.get(terminalId);

    int safeLimit = Math.max(1, Math.min(limit, 500));

    List<TerminalCommandEntity> base =
        (status == null)
            ? terminalCommandRepository.findByTerminalIdOrderByCreatedAtDesc(terminalId)
            : terminalCommandRepository.findByTerminalIdAndStatusOrderByCreatedAtDesc(terminalId, status);

    return base.stream()
        .sorted(Comparator.comparing(TerminalCommandEntity::getCreatedAt).reversed())
        .limit(safeLimit)
        .toList();
  }

  @Transactional
  public TerminalCommandEntity update(UUID commandId, TerminalCommandUpdateRequest req) {
    TerminalCommandEntity cmd = terminalCommandRepository.findById(commandId)
        .orElseThrow(() -> new NotFoundException("Command not found: " + commandId));

    // Minimal but safe lifecycle guard:
    // Once finalized -> cannot change status (except idempotent same value)
    if (isFinal(cmd.getStatus()) && req.getStatus() != cmd.getStatus()) {
      throw new IllegalStateException("Command already finalized: " + commandId);
    }

    cmd.setStatus(req.getStatus());

    if (req.getAcknowledgedAt() != null) cmd.setAcknowledgedAt(req.getAcknowledgedAt());
    if (req.getCompletedAt() != null) cmd.setCompletedAt(req.getCompletedAt());
    if (req.getErrorCode() != null) cmd.setErrorCode(req.getErrorCode());
    if (req.getErrorMessage() != null) cmd.setErrorMessage(req.getErrorMessage());

    cmd.setUpdatedAt(OffsetDateTime.now());
    return terminalCommandRepository.save(cmd);
  }

  private boolean isFinal(CommandStatus s) {
    return s == CommandStatus.SUCCEEDED
        || s == CommandStatus.FAILED
        || s == CommandStatus.CANCELED;
  }
}
