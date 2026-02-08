package com.oxalio.terne.repository;

import com.oxalio.terne.entity.TerminalEventEntity;
import com.oxalio.terne.model.TerminalEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TerminalEventRepository
    extends JpaRepository<TerminalEventEntity, UUID> {

  List<TerminalEventEntity>
      findTop100ByTerminalIdOrderByCapturedAtDesc(UUID terminalId);

  List<TerminalEventEntity>
      findByTerminalIdAndEventTypeOrderByCapturedAtDesc(
          UUID terminalId, TerminalEventType eventType);
}
