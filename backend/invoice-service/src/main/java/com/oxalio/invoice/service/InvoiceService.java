package com.oxalio.invoice.service;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.mapper.InvoiceMapper;
import com.oxalio.invoice.repository.InvoiceRepository;
import com.oxalio.invoice.service.QrCodeGenerator;
import com.oxalio.invoice.exception.InvoiceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des factures utilisant les DTOs et MapStruct.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final QrCodeGenerator qrCodeGenerator;

    /**
     * Crée une nouvelle facture.
     */
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        log.debug("Conversion du DTO en entité");
        InvoiceEntity entity = invoiceMapper.toEntity(request);

        // Génération des champs automatiques
        entity.setInvoiceNumber(generateInvoiceNumber());
        entity.setStickerId(generateStickerId());
        entity.setIssueDate(Instant.now());
        entity.setStatus("RECEIVED");

        // Génération du QR code
        String qrContent = buildQRContent(entity);
        entity.setQrBase64(qrCodeGenerator.generateQRCodeBase64(qrContent, 300, 300));

        log.debug("Sauvegarde de la facture en base de données");
        InvoiceEntity savedEntity = invoiceRepository.save(entity);

        log.info("Facture créée : {}", savedEntity.getInvoiceNumber());
        return invoiceMapper.toResponse(savedEntity);
    }

    /**
     * Récupère toutes les factures.
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        List<InvoiceEntity> entities = invoiceRepository.findAll();
        return invoiceMapper.toResponseList(entities);
    }

    /**
     * Récupère une facture par son ID.
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return invoiceMapper.toResponse(entity);
    }

    /**
     * Récupère une facture par son numéro.
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        InvoiceEntity entity = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException("numéro", invoiceNumber));
        return invoiceMapper.toResponse(entity);
    }

    /**
     * Met à jour une facture existante.
     */
    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {
        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        // Utilisation de la méthode de mise à jour du mapper
        invoiceMapper.updateEntityFromRequest(request, entity);

        // Régénération du QR code si nécessaire
        String qrContent = buildQRContent(entity);
        entity.setQrBase64(qrCodeGenerator.generateQRCodeBase64(qrContent, 300, 300));

        InvoiceEntity updatedEntity = invoiceRepository.save(entity);
        log.info("Facture mise à jour : {}", updatedEntity.getInvoiceNumber());
        
        return invoiceMapper.toResponse(updatedEntity);
    }

    /**
     * Supprime une facture.
     */
    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException("Facture non trouvée avec l'ID : " + id);
        }
        invoiceRepository.deleteById(id);
        log.info("Facture supprimée : ID {}", id);
    }

    /**
     * Soumet une facture à la DGI.
     * TODO: Implémenter l'intégration réelle avec l'API DGI FNE
     */
    @Transactional
    public InvoiceResponse submitToDgi(Long id) {
        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID : " + id));

        // Mock de la soumission DGI
        entity.setStatus("SUBMITTED_TO_DGI");
        entity.setDgiReference("DGI-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setDgiSubmittedAt(Instant.now());

        InvoiceEntity updatedEntity = invoiceRepository.save(entity);
        log.info("Facture soumise à la DGI : {} - Référence : {}", 
                updatedEntity.getInvoiceNumber(), updatedEntity.getDgiReference());

        return invoiceMapper.toResponse(updatedEntity);
    }

    /**
     * Génère un numéro de facture unique.
     * Format : INV-YYYY-NNNNNN
     */
    private String generateInvoiceNumber() {
        int year = Year.now().getValue();
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%d-%06d", year, count);
    }

    /**
     * Génère un identifiant de sticker unique.
     * Format : STKR-XXXXXXXX (8 caractères hexadécimaux)
     */
    private String generateStickerId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "STKR-" + uuid;
    }

    /**
     * Construit le contenu du QR code.
     */
    private String buildQRContent(InvoiceEntity entity) {
        return String.format(
                "Invoice: %s | Seller: %s | Buyer: %s | Amount: %s %s | Date: %s",
                entity.getInvoiceNumber(),
                entity.getSellerCompanyName(),
                entity.getBuyerName(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getIssueDate()
        );
    }
}