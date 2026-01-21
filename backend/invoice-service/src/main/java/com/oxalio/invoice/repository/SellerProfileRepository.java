// src/main/java/com/oxalio/invoice/repository/SellerProfileRepository.java
package com.oxalio.invoice.repository;

import com.oxalio.invoice.entity.SellerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfileEntity, Long> {

    Optional<SellerProfileEntity> findByTaxId(String taxId);
}
