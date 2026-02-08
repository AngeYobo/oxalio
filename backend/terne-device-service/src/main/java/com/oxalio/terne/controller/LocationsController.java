package com.oxalio.terne.controller;

import com.oxalio.terne.controller.dto.TerminalLocationIngestRequest;
import com.oxalio.terne.controller.dto.TerminalLocationResponse;
import com.oxalio.terne.entity.TerminalLocationEntity;
import com.oxalio.terne.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terne/terminals/{terminalId}")
@RequiredArgsConstructor
public class LocationsController {

  private final LocationService locationService;

  @PostMapping("/locations")
  public ResponseEntity<TerminalLocationResponse> ingest(
      @PathVariable UUID terminalId,
      @Valid @RequestBody TerminalLocationIngestRequest request
  ) {
    TerminalLocationEntity saved = locationService.ingest(terminalId, request);
    return ResponseEntity.ok(TerminalLocationResponse.from(saved));
  }

  @GetMapping("/locations")
  public ResponseEntity<List<TerminalLocationResponse>> history(
      @PathVariable UUID terminalId,
      @RequestParam(name = "from", required = false) OffsetDateTime from,
      @RequestParam(name = "to", required = false) OffsetDateTime to,
      @RequestParam(name = "limit", required = false, defaultValue = "100") int limit
  ) {
    List<TerminalLocationEntity> items = locationService.history(terminalId, from, to, limit);
    return ResponseEntity.ok(items.stream().map(TerminalLocationResponse::from).toList());
  }

  @GetMapping("/location/latest")
  public ResponseEntity<TerminalLocationResponse> latest(@PathVariable UUID terminalId) {
    return ResponseEntity.ok(TerminalLocationResponse.from(locationService.latest(terminalId)));
  }
}
