package com.oxalio.terne.repository;

import com.oxalio.terne.entity.TerminalEntity;
import com.oxalio.terne.model.TerminalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TerminalRepository extends JpaRepository<TerminalEntity, UUID> {

  Optional<TerminalEntity> findBySerialNumber(String serialNumber);

  List<TerminalEntity> findByTenantId(UUID tenantId);

  List<TerminalEntity> findByTenantIdAndStatus(UUID tenantId, TerminalStatus status);
}
