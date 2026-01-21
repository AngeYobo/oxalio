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
    
    // ════════════════════════════════════════════════════════════════
    // ✨ NOUVELLES MÉTHODES FNE (V8) - Pour système de refund
    // ════════════════════════════════════════════════════════════════
    
    /**
     * Chercher une facture par sa référence FNE.
     * 
     * Utilisé pour résoudre la référence DGI (ex: "2505842N26000000036")
     * vers l'ID UUID FNE nécessaire pour créer des refunds.
     * 
     * @param fneReference La référence DGI de la facture
     * @return Optional contenant la facture si trouvée
     */
    Optional<InvoiceEntity> findByFneReference(String fneReference);
    
    /**
     * Chercher une facture par son UUID FNE.
     * 
     * @param fneInvoiceId L'UUID FNE de la facture
     * @return Optional contenant la facture si trouvée
     */
    Optional<InvoiceEntity> findByFneInvoiceId(String fneInvoiceId);
}