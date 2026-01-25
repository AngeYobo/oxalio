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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.oxalio.invoice.repository.SellerProfileRepository;
import com.oxalio.invoice.service.SellerProfileService;
import java.io.ByteArrayOutputStream;
import java.util.Base64;



import java.util.Base64;


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


    // ============================================================
    // CREATE — FNE STRICT (TOT AUX CALCULÉS PAR LE BACKEND)
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
        entity.setQrBase64(qrCodeGenerator.generateQRCodeBase64(
                buildQRContent(entity), 300, 300));

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

        // --- GESTION DU TIMBRE DE QUITTANCE (Point 6 de la composition) ---
        // Le timbre de 100 CFA est obligatoire pour les paiements en espèces
        BigDecimal stampDuty = BigDecimal.ZERO;
        if ("cash".equalsIgnoreCase(entity.getPaymentMethod())) {
            stampDuty = BigDecimal.valueOf(100);
        }

        entity.setSubtotal(subtotal);
        entity.setTotalVat(totalVat);
        entity.setTotalAmount(subtotal.add(totalVat).add(stampDuty));
        
        // On affecte le timbre au champ otherTaxes pour la persistance
        entity.setOtherTaxes(stampDuty); 
        
        // Le total à payer inclut désormais : HT + TVA + TIMBRE
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

    @Transactional(readOnly = true)
    public byte[] generateFnePdf(Long id) throws Exception {
        InvoiceResponse inv = getInvoiceById(id);
        
        // Définition dynamique pour la conformité RNE/FNE
        boolean isRne = "B2C".equalsIgnoreCase(inv.getTemplate());
        String documentTitle = isRne ? "REÇU NORMALISÉ ÉLECTRONIQUE (RNE)" : "FACTURE NORMALISÉE FNE";
        String labelNumero = isRne ? "Reçu de vente N° : " : "Facture de vente N° : "; // Point 2

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float y = 780f;

                // 1) LOGO VENDEUR (HAUT DROITE)
                try {
                    String sellerTaxId = (inv.getSeller() != null) ? inv.getSeller().getTaxId() : null;
                    if (sellerTaxId != null && !sellerTaxId.isBlank()) {
                        byte[] logoBytes = sellerProfileService.getLogoBytes(sellerTaxId);
                        if (logoBytes != null && logoBytes.length > 0) {
                            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, logoBytes, "seller-logo");
                            c.drawImage(logo, pageWidth - 130f, y - 50f, 90f, 60f);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Impossible de charger le logo vendeur pour le document {} : {}", id, e.getMessage());
                }

                // 2) TITRE (Point 1 & 2)
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 16);
                c.newLineAtOffset(50, y);
                c.showText(documentTitle); // Visuel FNE/RNE
                c.endText();
                y -= 30;

                // 3) BLOC VENDEUR (Point 4)
                var s = inv.getSeller();
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 12);
                c.newLineAtOffset(50, y);
                c.showText("VENDEUR");
                c.endText();
                y -= 15;

                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 10);
                c.newLineAtOffset(50, y);
                c.showText(nvl(s != null ? s.getCompanyName() : null));
                if (isRne) { // Ajout du Terminal pour le RNE
                    c.newLineAtOffset(0, -12);
                    c.showText("TERMINAL : " + nvl(inv.getSeller() != null ? inv.getSeller().getPointOfSaleName() : null));
                }
                c.newLineAtOffset(0, -12);
                c.showText("NCC : " + nvl(s != null ? s.getTaxId() : null));
                c.endText();
                y -= 60;

                // 4) BLOC ACHETEUR (Point 5)
                var b = inv.getBuyer();
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 12);
                c.newLineAtOffset(50, y);
                c.showText("ACHETEUR");
                c.endText();
                y -= 15;

                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 10);
                c.newLineAtOffset(50, y);
                c.showText(nvl(b != null ? b.getName() : null));
                c.endText();
                y -= 40;

                // 5) INFOS DOCUMENT (Point 2)
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 10);
                c.newLineAtOffset(50, y);
                c.showText(labelNumero + nvl(inv.getInvoiceNumber())); // Numéro format spécial
                c.newLineAtOffset(0, -12);
                c.setFont(PDType1Font.HELVETICA, 10);
                c.showText("Date émission : " + (inv.getIssueDate() != null ? inv.getIssueDate().toString() : "—"));
                c.endText();
                y -= 40;

                // 6) TABLEAU PRODUITS
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 10);
                c.newLineAtOffset(50, y);
                c.showText("Désignation | Qté | P.U HT | TVA | Total TTC");
                c.endText();
                y -= 15;

                if (inv.getLines() != null) {
                    c.setFont(PDType1Font.HELVETICA, 10);
                    for (InvoiceResponse.InvoiceLineDTO line : inv.getLines()) {
                        c.beginText();
                        c.newLineAtOffset(50, y);
                        c.showText(nvl(line.getDescription()) + " | " + nbd(line.getQuantity()) + " | " + nbd(line.getUnitPrice()) + " | " + nbd(line.getVatAmount()) + " | " + nbd(line.getLineTotal()));
                        c.endText();
                        y -= 15;
                    }
                }
                y -= 20;

                // 7) TOTAUX & MODE DE PAIEMENT (Point 6 & 7)
                var t = inv.getTotals();
                BigDecimal subtotal = t != null ? safe(t.getSubtotal()) : BigDecimal.ZERO;
                BigDecimal vat = t != null ? safe(t.getTotalVat()) : BigDecimal.ZERO;
                BigDecimal other = t != null ? safe(t.getOtherTaxes()) : BigDecimal.ZERO;
                BigDecimal total = t != null ? safe(t.getTotalToPay()) : subtotal.add(vat).add(other);

                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 10);
                c.newLineAtOffset(50, y);
                c.showText("RÉSUMÉ DES MONTANTS");
                c.endText();
                y -= 15;

                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 10);
                c.newLineAtOffset(50, y);
                c.showText("Sous-total HT : " + nbd(subtotal));
                c.newLineAtOffset(0, -12);
                c.showText("TVA : " + nbd(vat));
                if (other.compareTo(BigDecimal.ZERO) > 0) {
                    c.newLineAtOffset(0, -12);
                    c.showText("TIMBRE DE QUITTANCE : " + nbd(other)); // Point 6
                }
                c.newLineAtOffset(0, -15);
                c.setFont(PDType1Font.HELVETICA_BOLD, 11);
                c.showText("TOTAL À PAYER TTC : " + nbd(total) + " CFA"); // Point 6
                c.newLineAtOffset(0, -15);
                c.showText("MODE DE PAIEMENT : " + (inv.getPaymentMode() != null ? inv.getPaymentMode() : "Espèces")); // Point 7
                c.endText();

                // 8) QR CODE DE CERTIFICATION (Point 3)
                if (inv.getQrBase64() != null && !inv.getQrBase64().isBlank()) {
                    try {
                        byte[] qrBytes = Base64.getDecoder().decode(inv.getQrBase64());
                        PDImageXObject qr = PDImageXObject.createFromByteArray(doc, qrBytes, "qr");
                        c.drawImage(qr, (pageWidth - 110) / 2, 80f, 110f, 110f); // Centrage
                    } catch (Exception e) {
                        log.warn("QR invalide : {}", e.getMessage());
                    }
                }
            } // Fin du try-with-resources ContentStream

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private static String nvl(String s) {
    return (s == null || s.isBlank()) ? "—" : s;
    }

    private static String nbd(BigDecimal b) {
        return b == null ? "0" : b.stripTrailingZeros().toPlainString();
    }



    @Transactional(readOnly = true)
    public byte[] generateMockPdf(Long id) throws Exception {
        return generateFnePdf(id);
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

    @Transactional
    public InvoiceResponse refundInvoice(Long id, com.oxalio.invoice.dto.RefundRequest refundRequest) {
        InvoiceEntity original = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        log.info("Traitement de l'avoir pour la facture : {} pour la raison : {}", 
                original.getInvoiceNumber(), refundRequest.getReason());

        // Logique simplifiée pour la DGI : On passe le statut en REFUNDED
        original.setStatus(InvoiceStatus.CANCELLED); 
        
        InvoiceEntity saved = invoiceRepository.save(original);
        return invoiceMapper.toResponse(saved);
    }
}
