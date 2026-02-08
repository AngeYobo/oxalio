package com.oxalio.invoice.service;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import com.oxalio.invoice.exception.InvoiceNotFoundException;
import com.oxalio.invoice.integration.DgiClientMock;
import com.oxalio.invoice.integration.DgiClientMock.DgiCertification;
import com.oxalio.invoice.mapper.InvoiceMapper;
import com.oxalio.invoice.model.InvoiceStatus;
import com.oxalio.invoice.repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final QrCodeGenerator qrCodeGenerator;
    private final DgiClientMock dgiClientMock;
    private final SellerProfileService sellerProfileService;
    private final HtmlPdfService htmlPdfService;

    // ============================================================
    // CREATE — FNE STRICT (TOTAUX CALCULÉS PAR LE BACKEND)
    // ============================================================
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        // Conversion partielle → entité sans lignes
        InvoiceEntity entity = invoiceMapper.toEntity(request);

        // Création des lignes
        List<InvoiceLineEntity> lines = invoiceMapper.toLineEntityList(request.getLines());
        lines.forEach(line -> {
            line.setInvoice(entity);

            BigDecimal base      = safe(line.getUnitPrice()).multiply(safe(line.getQuantity()));
            BigDecimal discount  = safe(line.getDiscount());
            BigDecimal vatRate   = safe(line.getVatRate());
            BigDecimal vatAmount = (base.subtract(discount))
                    .multiply(vatRate)
                    .divide(BigDecimal.valueOf(100));

            line.setVatAmount(vatAmount);
            line.setLineTotal(base.subtract(discount).add(vatAmount));
        });
        entity.setLines(lines);

        // Numéro facture
        entity.setInvoiceNumber(generateInvoiceNumber());
        entity.setIssueDate(Instant.now());
        entity.setStatus(InvoiceStatus.RECEIVED);
        entity.setStickerId(generateStickerId());

        // Calcul FNE automatique
        computeTotalsFne(entity);

        // QR code
        entity.setQrBase64(null);

        InvoiceEntity saved = invoiceRepository.save(entity);

        InvoiceResponse resp = invoiceMapper.toResponse(saved);
        resp.setLines(invoiceMapper.toLineResponseList(saved.getLines()));
        return resp;
    }

    // ============================================================
    // GET ALL
    // ============================================================
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceMapper.toResponseList(invoiceRepository.findAll());
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        InvoiceResponse resp = invoiceMapper.toResponse(entity);
        resp.setLines(invoiceMapper.toLineResponseList(entity.getLines()));
        resp.setTotals(computeTotalsResponse(resp));

        return resp;
    }

    // ============================================================
    // GET BY NUMBER
    // ============================================================
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        InvoiceEntity entity = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException("numéro", invoiceNumber));

        InvoiceResponse resp = invoiceMapper.toResponse(entity);
        resp.setLines(invoiceMapper.toLineResponseList(entity.getLines()));
        resp.setTotals(computeTotalsResponse(resp));

        return resp;
    }

    // ============================================================
    // UPDATE (PATCH)
    // ============================================================
    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {

        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        // apply patch
        invoiceMapper.updateEntityFromRequest(request, entity);

        // reset lignes
        entity.getLines().clear();
        List<InvoiceLineEntity> newLines = invoiceMapper.toLineEntityList(request.getLines());
        newLines.forEach(line -> {
            line.setInvoice(entity);

            BigDecimal base      = safe(line.getUnitPrice()).multiply(safe(line.getQuantity()));
            BigDecimal discount  = safe(line.getDiscount());
            BigDecimal vatRate   = safe(line.getVatRate());
            BigDecimal vatAmount = (base.subtract(discount))
                    .multiply(vatRate)
                    .divide(BigDecimal.valueOf(100));

            line.setVatAmount(vatAmount);
            line.setLineTotal(base.subtract(discount).add(vatAmount));
        });
        entity.getLines().addAll(newLines);

        // recalc totals
        computeTotalsFne(entity);

        // regenerate QR
        entity.setQrBase64(qrCodeGenerator.generateQRCodeBase64(
                buildQRContent(entity), 300, 300));

        InvoiceEntity updated = invoiceRepository.save(entity);

        InvoiceResponse resp = invoiceMapper.toResponse(updated);
        resp.setLines(invoiceMapper.toLineResponseList(updated.getLines()));
        resp.setTotals(computeTotalsResponse(resp));

        return resp;
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new InvoiceNotFoundException(id);
        }
        invoiceRepository.deleteById(id);
    }

    // ============================================================
    // SUBMIT → DGI MOCK
    // ============================================================
    @Transactional
    public InvoiceResponse submitToDgi(Long id) {

        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (entity.getDgiReference() != null) {
            return invoiceMapper.toResponse(entity);
        }

        DgiCertification cert = dgiClientMock.simulateCertification(entity);

        entity.setStatus(InvoiceStatus.SUBMITTED_TO_DGI);
        entity.setDgiReference(cert.dgiReference());
        entity.setStickerId(cert.stickerId());
        entity.setDgiSubmittedAt(Instant.now());
        entity.setQrBase64(cert.qrBase64());

        InvoiceEntity updated = invoiceRepository.save(entity);
        return invoiceMapper.toResponse(updated);
    }

    // ============================================================
    // BUSINESS LOGIC — CALCUL TOTALS FNE / RNE
    // ============================================================
    private void computeTotalsFne(InvoiceEntity entity) {

        BigDecimal subtotal  = BigDecimal.ZERO;
        BigDecimal totalVat  = BigDecimal.ZERO;
        BigDecimal discount  = BigDecimal.ZERO;

        for (InvoiceLineEntity l : entity.getLines()) {
            BigDecimal base = safe(l.getUnitPrice()).multiply(safe(l.getQuantity()));

            subtotal = subtotal.add(base.subtract(safe(l.getDiscount())));
            totalVat = totalVat.add(safe(l.getVatAmount()));
            discount = discount.add(safe(l.getDiscount()));
        }

        // --- GESTION DU TIMBRE DE QUITTANCE ---
        BigDecimal stampDuty = BigDecimal.ZERO;
        if ("cash".equalsIgnoreCase(entity.getPaymentMethod())) {
            stampDuty = BigDecimal.valueOf(100);
        }

        entity.setSubtotal(subtotal);
        entity.setTotalVat(totalVat);
        entity.setTotalAmount(subtotal.add(totalVat).add(stampDuty));
        entity.setOtherTaxes(stampDuty);
        entity.setTotalToPay(subtotal.add(totalVat).add(stampDuty));
    }

    // ============================================================
    // UTILS — BUILD TOTALS FOR RESPONSE
    // ============================================================
    private InvoiceResponse.TotalsDTO computeTotalsResponse(InvoiceResponse r) {

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal vat      = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        if (r.getLines() != null) {
            for (InvoiceResponse.InvoiceLineDTO l : r.getLines()) {
                BigDecimal base = safe(l.getUnitPrice())
                        .multiply(safe(l.getQuantity()));

                subtotal = subtotal.add(base.subtract(safe(l.getDiscount())));
                vat      = vat.add(safe(l.getVatAmount()));
                discount = discount.add(safe(l.getDiscount()));
            }
        }

        BigDecimal other = safe(r.getTotals() != null ? r.getTotals().getOtherTaxes() : BigDecimal.ZERO);

        return InvoiceResponse.TotalsDTO.builder()
                .subtotal(subtotal)
                .totalVat(vat)
                .totalAmount(subtotal.add(vat))
                .totalDiscount(discount)
                .otherTaxes(other)
                .totalToPay(subtotal.add(vat).add(other))
                .build();
    }

    // ============================================================
    // GENERATE FNE PDF (avec ou sans certification)
    // ============================================================
    @Transactional(readOnly = true)
    public byte[] generateFnePdf(Long invoiceId) {
        // 1. Récupérer la facture
        InvoiceEntity entity = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        
        // 2. Mapper vers InvoiceResponse
        InvoiceResponse response = invoiceMapper.toResponse(entity);
        response.setLines(invoiceMapper.toLineResponseList(entity.getLines()));
        response.setTotals(computeTotalsResponse(response));
        
        // 3. Si pas certifié FNE, ajouter une référence "BROUILLON"
        if (response.getFneReference() == null || response.getFneReference().isEmpty()) {
            response.setFneReference("BROUILLON-" + entity.getInvoiceNumber());
        }
        
        // 4. Générer le PDF
        return htmlPdfService.generatePdf(response);
    }

    // ============================================================
    // GENERATE MOCK PDF (preview sans certification)
    // ============================================================
    @Transactional(readOnly = true)
    public byte[] generateMockPdf(Long id) {
        InvoiceEntity entity = invoiceRepository.findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException(id));
        
        InvoiceResponse response = invoiceMapper.toResponse(entity);
        response.setLines(invoiceMapper.toLineResponseList(entity.getLines()));
        response.setTotals(computeTotalsResponse(response));
        
        return htmlPdfService.generatePdf(response);
    }

    // ============================================================
    // GET ENTITY BY ID (pour usage interne)
    // ============================================================
    @Transactional(readOnly = true)
    public InvoiceEntity getInvoiceEntityById(Long id) {
        return invoiceRepository.findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    // ============================================================
    // REFUND
    // ============================================================
    @Transactional
    public InvoiceResponse refundInvoice(Long id, com.oxalio.invoice.dto.RefundRequest refundRequest) {
        InvoiceEntity original = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        log.info("Traitement de l'avoir pour la facture : {} pour la raison : {}", 
                original.getInvoiceNumber(), refundRequest.getReason());

        original.setStatus(InvoiceStatus.CANCELLED);
        
        InvoiceEntity saved = invoiceRepository.save(original);
        return invoiceMapper.toResponse(saved);
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String generateInvoiceNumber() {
        int year = Year.now().getValue();
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%d-%06d", year, count);
    }

    private String generateStickerId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "STKR-" + uuid;
    }

    private String buildQRContent(InvoiceEntity e) {
        return "Invoice:" + e.getInvoiceNumber()
                + " | Seller:" + e.getSellerCompanyName()
                + " | Buyer:" + e.getBuyerName()
                + " | Amount:" + e.getTotalToPay()
                + " " + e.getCurrency()
                + " | Date:" + e.getIssueDate();
    }
}