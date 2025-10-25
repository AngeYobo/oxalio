package com.oxalio.invoice.repository;



import com.oxalio.invoice.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    
    /**
     * Recherche une facture par son numéro unique.
     */
    Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);
}