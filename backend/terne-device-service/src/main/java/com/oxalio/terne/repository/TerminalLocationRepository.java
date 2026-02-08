package com.oxalio.terne.repository;

import com.oxalio.terne.entity.TerminalLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TerminalLocationRepository extends JpaRepository<TerminalLocationEntity, UUID> {

  List<TerminalLocationEntity>
      findTop100ByTerminalIdOrderByCapturedAtDesc(UUID terminalId);

  Optional<TerminalLocationEntity>
      findFirstByTerminalIdOrderByCapturedAtDesc(UUID terminalId);
}
