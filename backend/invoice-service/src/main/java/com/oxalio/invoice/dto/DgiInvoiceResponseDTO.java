package com.oxalio.invoice.dto;

import java.time.Instant;

/**
 * DTO pour la réponse reçue depuis la DGI (FNE).
 * Exemple typique à adapter selon la spec officielle.
 */
public class DgiInvoiceResponseDTO {

    private String reference;       // Numéro de référence DGI
    private String status;          // VALIDATED / REJECTED
    private String message;         // Message texte de la DGI
    private String signature;       // Signature électronique ou hash
    private String qrCode;          // QR code encodé (base64 ou URL)
    private Instant processedAt;    // Horodatage de la réponse

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
