package com.oxalio.invoice.integration;

import com.oxalio.invoice.entity.InvoiceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Client Mock pour l'intégration avec l'API DGI FNE.
 * Cette implémentation simule les appels à la DGI en attendant l'intégration réelle.
 * 
 * À remplacer par DgiClientImpl lors de l'intégration avec l'API DGI sandbox/production.
 */
@Slf4j
@Component
public class DgiClientMock {

    /**
     * Simule la soumission d'une facture à la DGI.
     * 
     * @param invoiceNumber Numéro de la facture à soumettre
     * @return Réponse mock de la DGI
     */
    public DgiSubmissionResponse submitInvoice(String invoiceNumber) {
        log.info("📤 [MOCK] Soumission de la facture {} à la DGI", invoiceNumber);
        
        // Simuler un délai réseau
        simulateNetworkDelay();
        
        // Générer une référence DGI mock
        String dgiReference = "DGI-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("✅ [MOCK] Facture {} acceptée par la DGI. Référence: {}", invoiceNumber, dgiReference);
        
        return DgiSubmissionResponse.builder()
                .success(true)
                .dgiReference(dgiReference)
                .submissionDate(Instant.now())
                .message("Facture acceptée (MOCK)")
                .build();
    }

    /**
     * Simule la certification d'une facture auprès de la DGI.
     * 
     * @param invoiceNumber Numéro de la facture à certifier
     * @return Certification mock de la DGI
     */
    public DgiCertification certifyInvoice(String invoiceNumber) {
        log.info("🔐 [MOCK] Certification de la facture {} auprès de la DGI", invoiceNumber);
        
        // Simuler un délai réseau
        simulateNetworkDelay();
        
        // Générer une certification mock
        String certificationId = "CERT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String dgiReference = "DGI-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String stickerId = "STKR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String qrCodeData = "QR-" + UUID.randomUUID().toString();
        String qrBase64 = generateMockQrCode();
        
        log.info("✅ [MOCK] Facture {} certifiée. ID Certification: {}", invoiceNumber, certificationId);
        
        return DgiCertification.builder()
                .certificationId(certificationId)
                .invoiceNumber(invoiceNumber)
                .dgiReference(dgiReference)
                .stickerId(stickerId)
                .certifiedAt(Instant.now())
                .qrCodeData(qrCodeData)
                .qrBase64(qrBase64)
                .status("CERTIFIED")
                .message("Facture certifiée par la DGI (MOCK)")
                .build();
    }

    /**
     * Simule la certification d'une facture avec l'entité complète.
     * Cette méthode est appelée par InvoiceService.submitToDgi()
     * 
     * @param invoice L'entité facture à certifier
     * @return Certification mock de la DGI
     */
    public DgiCertification simulateCertification(InvoiceEntity invoice) {
        log.info("🔐 [MOCK] Certification de la facture {} auprès de la DGI", invoice.getInvoiceNumber());
        return certifyInvoice(invoice.getInvoiceNumber());
    }

    /**
     * Simule la signature d'une facture (alias pour simulateCertification).
     * 
     * @param invoice L'entité facture à signer
     * @return Certification mock de la DGI
     */
    public DgiCertification signInvoice(InvoiceEntity invoice) {
        log.info("✍️ [MOCK] Signature de la facture: {}", invoice.getInvoiceNumber());
        return certifyInvoice(invoice.getInvoiceNumber());
    }

    /**
     * Simule la vérification du statut d'une facture auprès de la DGI.
     * 
     * @param dgiReference Référence DGI de la facture
     * @return Statut mock de la facture
     */
    public DgiStatusResponse checkStatus(String dgiReference) {
        log.info("🔍 [MOCK] Vérification du statut pour la référence DGI: {}", dgiReference);
        
        simulateNetworkDelay();
        
        return DgiStatusResponse.builder()
                .dgiReference(dgiReference)
                .status("ACCEPTED")
                .message("Facture validée par la DGI (MOCK)")
                .lastUpdated(Instant.now())
                .build();
    }

    /**
     * Simule l'annulation d'une facture auprès de la DGI.
     * 
     * @param dgiReference Référence DGI de la facture à annuler
     * @return Réponse mock de l'annulation
     */
    public DgiCancellationResponse cancelInvoice(String dgiReference) {
        log.info("❌ [MOCK] Annulation de la facture avec référence DGI: {}", dgiReference);
        
        simulateNetworkDelay();
        
        return DgiCancellationResponse.builder()
                .success(true)
                .dgiReference(dgiReference)
                .cancellationDate(Instant.now())
                .message("Facture annulée (MOCK)")
                .build();
    }

    /**
     * Génère un QR code mock en Base64.
     */
    private String generateMockQrCode() {
        // Retourne un petit PNG transparent encodé en Base64 (1x1 pixel)
        return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    /**
     * Simule un délai réseau (100-300ms).
     */
    private void simulateNetworkDelay() {
        try {
            long delay = 100 + (long) (Math.random() * 200);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Délai simulé interrompu", e);
        }
    }

    /**
     * DTO pour la réponse de soumission DGI.
     */
    public record DgiSubmissionResponse(
        boolean success,
        String dgiReference,
        Instant submissionDate,
        String message,
        String errorCode
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean success;
            private String dgiReference;
            private Instant submissionDate;
            private String message;
            private String errorCode;

            public Builder success(boolean success) { this.success = success; return this; }
            public Builder dgiReference(String dgiReference) { this.dgiReference = dgiReference; return this; }
            public Builder submissionDate(Instant submissionDate) { this.submissionDate = submissionDate; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
            public DgiSubmissionResponse build() {
                return new DgiSubmissionResponse(success, dgiReference, submissionDate, message, errorCode);
            }
        }
    }

    /**
     * DTO pour la certification DGI.
     */
    public record DgiCertification(
        String certificationId,
        String invoiceNumber,
        String dgiReference,
        String stickerId,
        Instant certifiedAt,
        String qrCodeData,
        String qrBase64,
        String status,
        String message,
        String errorCode
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String certificationId;
            private String invoiceNumber;
            private String dgiReference;
            private String stickerId;
            private Instant certifiedAt;
            private String qrCodeData;
            private String qrBase64;
            private String status;
            private String message;
            private String errorCode;

            public Builder certificationId(String certificationId) { this.certificationId = certificationId; return this; }
            public Builder invoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; return this; }
            public Builder dgiReference(String dgiReference) { this.dgiReference = dgiReference; return this; }
            public Builder stickerId(String stickerId) { this.stickerId = stickerId; return this; }
            public Builder certifiedAt(Instant certifiedAt) { this.certifiedAt = certifiedAt; return this; }
            public Builder qrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; return this; }
            public Builder qrBase64(String qrBase64) { this.qrBase64 = qrBase64; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
            
            public DgiCertification build() {
                return new DgiCertification(certificationId, invoiceNumber, dgiReference, stickerId,
                    certifiedAt, qrCodeData, qrBase64, status, message, errorCode);
            }
        }
    }

    /**
     * DTO pour la réponse de vérification de statut DGI.
     */
    public record DgiStatusResponse(
        String dgiReference,
        String status,
        String message,
        Instant lastUpdated
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String dgiReference;
            private String status;
            private String message;
            private Instant lastUpdated;

            public Builder dgiReference(String dgiReference) { this.dgiReference = dgiReference; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder lastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; return this; }
            
            public DgiStatusResponse build() {
                return new DgiStatusResponse(dgiReference, status, message, lastUpdated);
            }
        }
    }

    /**
     * DTO pour la réponse d'annulation DGI.
     */
    public record DgiCancellationResponse(
        boolean success,
        String dgiReference,
        Instant cancellationDate,
        String message,
        String errorCode
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean success;
            private String dgiReference;
            private Instant cancellationDate;
            private String message;
            private String errorCode;

            public Builder success(boolean success) { this.success = success; return this; }
            public Builder dgiReference(String dgiReference) { this.dgiReference = dgiReference; return this; }
            public Builder cancellationDate(Instant cancellationDate) { this.cancellationDate = cancellationDate; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
            
            public DgiCancellationResponse build() {
                return new DgiCancellationResponse(success, dgiReference, cancellationDate, message, errorCode);
            }
        }
    }
}