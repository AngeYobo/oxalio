package com.oxalio.terne.repository;

import com.oxalio.terne.entity.TerminalCommandEntity;
import com.oxalio.terne.model.CommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TerminalCommandRepository
    extends JpaRepository<TerminalCommandEntity, UUID> {

  List<TerminalCommandEntity>
      findByTerminalIdOrderByCreatedAtDesc(UUID terminalId);

  List<TerminalCommandEntity>
      findByTerminalIdAndStatusOrderByCreatedAtDesc(UUID terminalId, CommandStatus status);
}
