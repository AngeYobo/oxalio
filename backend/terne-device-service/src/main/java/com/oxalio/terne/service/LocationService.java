package com.oxalio.terne.service;

import com.oxalio.terne.controller.dto.TerminalLocationIngestRequest;
import com.oxalio.terne.entity.TerminalLocationEntity;
import com.oxalio.terne.exception.NotFoundException;
import com.oxalio.terne.repository.TerminalLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final TerminalService terminalService;
  private final TerminalLocationRepository terminalLocationRepository;

  @Transactional
  public TerminalLocationEntity ingest(UUID terminalId, TerminalLocationIngestRequest req) {
    // Ensure terminal exists (audit)
    terminalService.get(terminalId);

    TerminalLocationEntity e = TerminalLocationEntity.builder()
        .id(UUID.randomUUID())
        .terminalId(terminalId)
        .capturedAt(req.getCapturedAt())
        .source(req.getSource())
        .latitude(req.getLatitude())
        .longitude(req.getLongitude())
        .accuracyMeters(req.getAccuracyMeters())
        .provider(req.getProvider())
        .createdAt(OffsetDateTime.now())
        .build();

    return terminalLocationRepository.save(e);
  }

  @Transactional(readOnly = true)
  public List<TerminalLocationEntity> history(UUID terminalId, OffsetDateTime from, OffsetDateTime to, int limit) {
    terminalService.get(terminalId);

    int safeLimit = Math.max(1, Math.min(limit, 1000));

    // Minimal implementation based on existing repository methods.
    // For large volumes, replace with a real query + Pageable.
    List<TerminalLocationEntity> base =
        terminalLocationRepository.findTop100ByTerminalIdOrderByCapturedAtDesc(terminalId);

    return base.stream()
        .filter(x -> from == null || !x.getCapturedAt().isBefore(from))
        .filter(x -> to == null || !x.getCapturedAt().isAfter(to))
        .sorted(Comparator.comparing(TerminalLocationEntity::getCapturedAt).reversed())
        .limit(safeLimit)
        .toList();
  }

  @Transactional(readOnly = true)
  public TerminalLocationEntity latest(UUID terminalId) {
    terminalService.get(terminalId);

    return terminalLocationRepository.findFirstByTerminalIdOrderByCapturedAtDesc(terminalId)
        .orElseThrow(() -> new NotFoundException("No location found for terminal: " + terminalId));
  }
}
