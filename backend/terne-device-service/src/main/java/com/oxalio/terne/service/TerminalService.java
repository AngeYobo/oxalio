package com.oxalio.terne.service;

import com.oxalio.terne.controller.dto.TerminalEnrollRequest;
import com.oxalio.terne.controller.dto.TerminalUpdateRequest;
import com.oxalio.terne.entity.TerminalEntity;
import com.oxalio.terne.exception.NotFoundException;
import com.oxalio.terne.model.TerminalStatus;
import com.oxalio.terne.repository.TerminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TerminalService {

  private final TerminalRepository terminalRepository;

  @Transactional
  public TerminalEntity enroll(TerminalEnrollRequest req) {
    terminalRepository.findBySerialNumber(req.getSerialNumber()).ifPresent(existing -> {
      throw new IllegalStateException("Terminal already enrolled with serialNumber=" + req.getSerialNumber());
    });

    OffsetDateTime now = OffsetDateTime.now();

    TerminalEntity t = TerminalEntity.builder()
        // DB has DEFAULT gen_random_uuid(); but we generate in app to keep deterministic behavior.
        .id(UUID.randomUUID())
        .tenantId(req.getTenantId())
        .posId(req.getPosId())
        .status(TerminalStatus.ENROLLED)
        .serialNumber(req.getSerialNumber())
        .imei(req.getImei())
        .manufacturer(req.getManufacturer())
        .model(req.getModel())
        .osVersion(req.getOsVersion())
        .appVersion(req.getAppVersion())
        .mdmProvider(req.getMdmProvider())
        .mdmDeviceId(req.getMdmDeviceId())
        // tags stored as TEXT[] in PostgreSQL
        .tags(req.getTags() == null ? null : req.getTags().toArray(new String[0]))
        .lastSeenAt(null)
        .createdAt(now)
        .updatedAt(now)
        .build();

    return terminalRepository.save(t);
  }

  @Transactional(readOnly = true)
  public TerminalEntity get(UUID terminalId) {
    return terminalRepository.findById(terminalId)
        .orElseThrow(() -> new NotFoundException("Terminal not found: " + terminalId));
  }

  @Transactional
  public TerminalEntity update(UUID terminalId, TerminalUpdateRequest req) {
    TerminalEntity t = get(terminalId);

    // PATCH semantics: update only non-null fields
    if (req.getTenantId() != null) t.setTenantId(req.getTenantId());
    if (req.getPosId() != null) t.setPosId(req.getPosId());
    if (req.getMdmProvider() != null) t.setMdmProvider(req.getMdmProvider());
    if (req.getMdmDeviceId() != null) t.setMdmDeviceId(req.getMdmDeviceId());
    if (req.getAppVersion() != null) t.setAppVersion(req.getAppVersion());
    if (req.getTags() != null) t.setTags(req.getTags().toArray(new String[0]));

    t.setUpdatedAt(OffsetDateTime.now());
    return terminalRepository.save(t);
  }

  @Transactional
  public TerminalEntity activate(UUID terminalId) {
    TerminalEntity t = get(terminalId);

    if (t.getStatus() == TerminalStatus.RETIRED) {
      throw new IllegalStateException("Cannot activate a retired terminal: " + terminalId);
    }
    // Allowed: ENROLLED -> ACTIVE, SUSPENDED -> ACTIVE, ACTIVE -> ACTIVE (idempotent)
    t.setStatus(TerminalStatus.ACTIVE);
    t.setUpdatedAt(OffsetDateTime.now());
    return terminalRepository.save(t);
  }

  @Transactional
  public TerminalEntity suspend(UUID terminalId) {
    TerminalEntity t = get(terminalId);

    if (t.getStatus() == TerminalStatus.RETIRED) {
      throw new IllegalStateException("Cannot suspend a retired terminal: " + terminalId);
    }
    // Allowed: ENROLLED -> SUSPENDED, ACTIVE -> SUSPENDED, SUSPENDED -> SUSPENDED (idempotent)
    t.setStatus(TerminalStatus.SUSPENDED);
    t.setUpdatedAt(OffsetDateTime.now());
    return terminalRepository.save(t);
  }

  @Transactional
  public void touchLastSeen(UUID terminalId, OffsetDateTime seenAt) {
    TerminalEntity t = get(terminalId);
    t.setLastSeenAt(seenAt);
    t.setUpdatedAt(OffsetDateTime.now());
    terminalRepository.save(t);
  }
}
