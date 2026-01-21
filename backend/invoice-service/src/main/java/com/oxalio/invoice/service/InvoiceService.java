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
    // BUSINESS LOGIC — CALCUL TOTALS FNE
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

        // otherTaxes éventuel (pour l’instant = 0)
        BigDecimal other = safe(entity.getOtherTaxes());

        entity.setSubtotal(subtotal);
        entity.setTotalVat(totalVat);
        entity.setTotalAmount(subtotal.add(totalVat));
        entity.setOtherTaxes(other);
        entity.setTotalToPay(subtotal.add(totalVat).add(other));
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

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {

                float pageWidth = page.getMediaBox().getWidth();
                float y = 780f;

                // ==========================
                // 1) LOGO VENDEUR (HAUT DROITE)
                // ==========================
                try {
                    String sellerTaxId = (inv.getSeller() != null) ? inv.getSeller().getTaxId() : null;
                    if (sellerTaxId != null && !sellerTaxId.isBlank()) {
                        byte[] logoBytes = sellerProfileService.getLogoBytes(sellerTaxId);
                        if (logoBytes != null && logoBytes.length > 0) {
                            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, logoBytes, "seller-logo");
                            float logoWidth = 90f;
                            float logoHeight = 60f;
                            float logoX = pageWidth - logoWidth - 40f;
                            float logoY = y - logoHeight + 10f;
                            c.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Impossible de charger le logo vendeur pour la facture {} : {}", id, e.getMessage());
                }


                // ==========================
                // 2) TITRE
                // ==========================
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 18);
                c.newLineAtOffset(50, y);
                c.showText("FACTURE NORMALISÉE FNE");
                c.endText();

                y -= 30;

                // ==========================
                // 3) BLOC VENDEUR
                // ==========================
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
                c.newLineAtOffset(0, -12);
                c.showText("NCC : " + nvl(s != null ? s.getTaxId() : null));
                c.newLineAtOffset(0, -12);
                c.showText("Adresse : " + nvl(s != null ? s.getAddress() : null));
                c.newLineAtOffset(0, -12);
                c.showText("Téléphone : " + nvl(s != null ? s.getPhone() : null));
                c.newLineAtOffset(0, -12);
                c.showText("Email : " + nvl(s != null ? s.getEmail() : null));
                c.endText();

                y -= 70;

                // ==========================
                // 4) BLOC ACHETEUR
                // ==========================
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
                c.newLineAtOffset(0, -12);
                c.showText("NCC : " + nvl(b != null ? b.getTaxId() : null));
                c.newLineAtOffset(0, -12);
                c.showText("Adresse : " + nvl(b != null ? b.getAddress() : null));
                c.newLineAtOffset(0, -12);
                c.showText("Téléphone : " + nvl(b != null ? b.getPhone() : null));
                c.newLineAtOffset(0, -12);
                c.showText("Email : " + nvl(b != null ? b.getEmail() : null));
                c.endText();

                y -= 70;

                // ==========================
                // 5) INFOS FACTURE
                // ==========================
                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 10);
                c.newLineAtOffset(50, y);
                c.showText("Numéro facture : " + nvl(inv.getInvoiceNumber()));
                c.newLineAtOffset(0, -12);
                c.showText("Date émission : " + (inv.getIssueDate() != null ? inv.getIssueDate().toString() : "—"));
                c.newLineAtOffset(0, -12);
                c.showText("Sticker ID : " + nvl(inv.getStickerId()));
                c.newLineAtOffset(0, -12);
                c.showText("Réf. DGI : " + nvl(inv.getDgiReference()));
                c.endText();

                y -= 60;

                // ==========================
                // 6) TABLEAU PRODUITS (simple pour l’instant)
                // ==========================
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 10);
                c.newLineAtOffset(50, y);
                c.showText("Désignation | Qté | P.U HT | TVA | Total TTC");
                c.endText();

                y -= 15;
                c.setFont(PDType1Font.HELVETICA, 10);

                if (inv.getLines() != null) {
                    for (InvoiceResponse.InvoiceLineDTO line : inv.getLines()) {
                        c.beginText();
                        c.newLineAtOffset(50, y);
                        c.showText(
                                nvl(line.getDescription()) + " | " +
                                nbd(line.getQuantity()) + " | " +
                                nbd(line.getUnitPrice()) + " | " +
                                nbd(line.getVatAmount()) + " | " +
                                nbd(line.getLineTotal())
                        );
                        c.endText();
                        y -= 15;
                        if (y < 120) break; // pour éviter de descendre trop bas sur 1 page
                    }
                }

                y -= 30;

                // ==========================
                // 7) TOTAUX
                // ==========================
                var t = inv.getTotals();
                BigDecimal subtotal = t != null && t.getSubtotal() != null ? t.getSubtotal() : BigDecimal.ZERO;
                BigDecimal vat      = t != null && t.getTotalVat() != null ? t.getTotalVat() : BigDecimal.ZERO;
                BigDecimal other    = t != null && t.getOtherTaxes() != null ? t.getOtherTaxes() : BigDecimal.ZERO;
                BigDecimal total    = t != null && t.getTotalToPay() != null ? t.getTotalToPay() : subtotal.add(vat).add(other);

                c.beginText();
                c.setFont(PDType1Font.HELVETICA_BOLD, 10);
                c.newLineAtOffset(50, y);
                c.showText("Résumé des montants");
                c.endText();

                y -= 15;

                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 10);
                c.newLineAtOffset(50, y);
                c.showText("Sous-total HT : " + subtotal);
                c.newLineAtOffset(0, -12);
                c.showText("TVA : " + vat);
                c.newLineAtOffset(0, -12);
                c.showText("Autres taxes : " + other);
                c.newLineAtOffset(0, -12);
                c.showText("TOTAL À PAYER : " + total);
                c.endText();

                // ==========================
                // 8) QR CODE FNE (Base64) en bas à droite
                // ==========================
                if (inv.getQrBase64() != null && !inv.getQrBase64().isBlank()) {
                    try {
                        byte[] qrBytes = Base64.getDecoder().decode(inv.getQrBase64());
                        PDImageXObject qr = PDImageXObject.createFromByteArray(doc, qrBytes, "qr");
                        float qrSize = 110f;
                        float qrX = pageWidth - qrSize - 40f;
                        float qrY = 80f;
                        c.drawImage(qr, qrX, qrY, qrSize, qrSize);
                    } catch (Exception e) {
                        log.warn("QR invalide pour la facture {} : {}", id, e.getMessage());
                    }
                }

                // ==========================
                // 9) FOOTER
                // ==========================
                c.beginText();
                c.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                c.newLineAtOffset(50, 50);
                c.showText("Facture normalisée conforme FNE – Oxalio Plateform (génération mock)");
                c.endText();
            }

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
}
