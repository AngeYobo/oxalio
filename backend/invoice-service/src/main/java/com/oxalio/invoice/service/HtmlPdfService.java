package com.oxalio.invoice.service;

import com.lowagie.text.DocumentException;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.dto.InvoiceResponse.InvoiceLineDTO;
import com.oxalio.invoice.dto.InvoiceResponse.SellerDTO;
import com.oxalio.invoice.dto.InvoiceResponse.BuyerDTO;
import com.oxalio.invoice.dto.InvoiceResponse.TotalsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service de génération de PDF pour les factures FNE (Facture Normalisée Électronique)
 * conforme au design officiel de la DGI Côte d'Ivoire.
 * 
 * CALCULS CONFORMES FNE :
 * - Montant HT ligne = Prix Unitaire × Quantité (SANS TVA)
 * - Total HT = Somme des Montants HT lignes
 * - TVA = Total HT × Taux TVA
 * - Total TTC = Total HT + TVA
 * - Timbre = 100 FCFA si paiement cash
 * - Total à payer = Total TTC + Timbre
 */
@Service
public class HtmlPdfService {

    private static final Logger log = LoggerFactory.getLogger(HtmlPdfService.class);

    private static final String TEMPLATE_PATH = "templates/invoice-template.html";
    private static final String FNE_STICKER_PATH = "static/fne-logo.png";
    private static final String DEFAULT_LOGO_PATH = "static/default-logo.png";
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("Africa/Abidjan"));
    
    private static final DecimalFormat AMOUNT_FORMAT;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        AMOUNT_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    @Value("${oxalio.seller-logos-path:seller-logos/}")
    private String sellerLogosPath;
    
    @Value("${oxalio.default-regime:TEE}")
    private String defaultRegime;
    
    @Value("${oxalio.default-tax-center:838 Impôts de Bouake i}")
    private String defaultTaxCenter;

    private String templateHtml;
    private String fneStickerBase64;
    private String defaultLogoBase64;

    @PostConstruct
    public void init() {
        log.info("🚀 Initialisation HtmlPdfService...");
        loadTemplate();
        loadFneSticker();
        loadDefaultLogo();
        log.info("✅ HtmlPdfService initialisé avec succès");
    }

    private void loadTemplate() {
        try {
            Resource resource = new ClassPathResource(TEMPLATE_PATH);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    templateHtml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    log.info("📄 Template HTML chargé ({} caractères)", templateHtml.length());
                    return;
                }
            }
        } catch (IOException e) {
            log.warn("⚠️ Erreur chargement template: {}", e.getMessage());
        }
        
        log.warn("⚠️ Template non trouvé: {} - utilisation du template par défaut", TEMPLATE_PATH);
        templateHtml = getDefaultTemplate();
    }

    private void loadFneSticker() {
        try {
            Resource resource = new ClassPathResource(FNE_STICKER_PATH);
            if (resource.exists()) {
                fneStickerBase64 = encodeResourceToBase64(resource, "image/png");
                log.info("🏷️ Sticker FNE chargé");
                return;
            }
        } catch (IOException e) {
            log.warn("⚠️ Erreur chargement sticker FNE: {}", e.getMessage());
        }
        
        log.warn("⚠️ Sticker FNE non trouvé - utilisation placeholder");
        fneStickerBase64 = createPlaceholderImage();
    }

    private void loadDefaultLogo() {
        try {
            Resource resource = new ClassPathResource(DEFAULT_LOGO_PATH);
            if (resource.exists()) {
                defaultLogoBase64 = encodeResourceToBase64(resource, "image/png");
                return;
            }
        } catch (IOException e) {
            log.warn("⚠️ Erreur chargement logo par défaut: {}", e.getMessage());
        }
        
        defaultLogoBase64 = createPlaceholderImage();
    }

    /**
     * Génère un PDF de facture FNE à partir des données de facture.
     */
    public byte[] generatePdf(InvoiceResponse invoice) {
        log.info("📑 Génération PDF pour facture: {}", invoice.getFneReference());
        
        try {
            String html = buildHtml(invoice);
            return convertHtmlToPdf(html);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du PDF pour {}", invoice.getFneReference(), e);
            throw new PdfGenerationException("Échec de génération du PDF", e);
        }
    }

    private String buildHtml(InvoiceResponse invoice) {
        String html = templateHtml;
        
        SellerDTO seller = invoice.getSeller();
        BuyerDTO buyer = invoice.getBuyer();
        
        // ════════════════════════════════════════════════════════════════
        // RECALCULER LES TOTAUX COMME FNE
        // ════════════════════════════════════════════════════════════════
        CalculatedTotals totals = calculateTotalsLikeFne(invoice);
        
        // INFORMATIONS VENDEUR (HEADER)
        html = html.replace("{{SELLER_COMPANY_NAME}}", 
                escapeHtml(nullSafe(seller != null ? seller.getCompanyName() : null)));
        html = html.replace("{{SELLER_NCC}}", 
                escapeHtml(nullSafe(seller != null ? seller.getTaxId() : null)));
        html = html.replace("{{SELLER_REGIME}}", 
                escapeHtml(getSellerRegime(seller)));
        html = html.replace("{{SELLER_TAX_CENTER}}", 
                escapeHtml(getSellerTaxCenter(seller)));
        
        // LOGO VENDEUR
        html = html.replace("{{SELLER_LOGO_BASE64}}", 
                getSellerLogoBase64(seller != null ? seller.getTaxId() : null));
        
        // DÉTAILS VENDEUR
        html = html.replace("{{SELLER_RCCM}}", 
                escapeHtml(nullSafe(seller != null ? seller.getRccm() : null)));
        html = html.replace("{{SELLER_BANK_REF}}", 
                escapeHtml(nullSafe(seller != null ? seller.getBankRef() : null)));
        html = html.replace("{{SELLER_ESTABLISHMENT}}", 
                escapeHtml(nullSafe(seller != null ? seller.getEstablishment() : null, 
                        seller != null ? seller.getCompanyName() : null)));
        html = html.replace("{{SELLER_ADDRESS}}", 
                escapeHtml(nullSafe(seller != null ? seller.getAddress() : null)));
        html = html.replace("{{SELLER_PHONE}}", 
                escapeHtml(nullSafe(seller != null ? seller.getPhone() : null)));
        html = html.replace("{{SELLER_EMAIL}}", 
                escapeHtml(nullSafe(seller != null ? seller.getEmail() : null)));
        html = html.replace("{{SELLER_NAME}}", 
                escapeHtml(nullSafe(seller != null ? seller.getSellerDisplayName() : null)));
        html = html.replace("{{SELLER_POS_NAME}}", 
                escapeHtml(nullSafe(seller != null ? seller.getPointOfSaleName() : null)));
        
        // DATE ET PAIEMENT
        html = html.replace("{{INVOICE_DATE_TIME}}", 
                formatDateTime(invoice.getIssueDate()));
        html = html.replace("{{PAYMENT_MODE}}", 
                escapeHtml(translatePaymentMode(invoice.getPaymentMode())));
        
        // MESSAGE COMMERCIAL
        html = html.replace("{{COMMERCIAL_MESSAGE}}", 
                escapeHtml(nullSafe(invoice.getNotes(), "Merci pour votre confiance")));
        
        // RÉFÉRENCE FACTURE
        html = html.replace("{{FNE_REFERENCE}}", 
                escapeHtml(nullSafe(invoice.getFneReference())));
        
        // QR CODE
        html = html.replace("{{QR_CODE_BASE64}}", 
                formatBase64Image(invoice.getQrBase64(), "image/png"));
        
        // STICKER FNE
        html = html.replace("{{FNE_STICKER_BASE64}}", fneStickerBase64);
        
        // INFORMATIONS CLIENT
        html = html.replace("{{BUYER_NAME}}", 
                escapeHtml(nullSafe(buyer != null ? buyer.getName() : null)));
        html = html.replace("{{BUYER_ADDRESS}}", 
                escapeHtml(nullSafe(buyer != null ? buyer.getAddress() : null, 
                        buyer != null ? buyer.getEmail() : null)));
        html = html.replace("{{BUYER_NCC}}",
                escapeHtml(nullSafe(buyer != null ? buyer.getTaxId() : null)));

        // INFORMATIONS CLIENT - Régime optionnel (ligne entière)
        String buyerRegime = nullSafe(buyer != null ? buyer.getRegime() : null);
        if (!buyerRegime.isBlank()) {
            html = html.replace("{{BUYER_REGIME_LINE}}",
                    "<span class=\"label\">Régime d'imposition :</span> " + escapeHtml(buyerRegime) + "<br/>");
        } else {
            html = html.replace("{{BUYER_REGIME_LINE}}", "");
        }
        
        // LIGNES DE FACTURE (avec calculs HT corrects)
        html = html.replace("{{INVOICE_LINES}}", buildInvoiceLines(invoice.getLines()));
        
        // TOTAUX (calculés comme FNE)
        html = html.replace("{{TOTAL_HT}}", formatAmount(totals.totalHT));
        html = html.replace("{{TOTAL_TVA}}", formatAmount(totals.totalTVA));
        html = html.replace("{{TOTAL_TTC}}", formatAmount(totals.totalTTC));
        html = html.replace("{{OTHER_TAXES}}", formatAmount(totals.otherTaxes));
        html = html.replace("{{STAMP_TAX}}", formatAmount(totals.stampTax));
        html = html.replace("{{TOTAL_TO_PAY}}", formatAmount(totals.totalToPay));
        
        // RÉSUMÉ TVA
        html = html.replace("{{TAX_SUMMARY_LINES}}", buildTaxSummary(invoice.getLines(), totals));
        
        return html;
    }

    /**
     * Calcule les totaux exactement comme FNE :
     * - Montant HT = Prix Unitaire × Quantité (SANS TVA)
     * - Total HT = Somme des montants HT
     * - TVA = Total HT × 18%
     * - Timbre = 100 si cash
     */
    private CalculatedTotals calculateTotalsLikeFne(InvoiceResponse invoice) {
        CalculatedTotals result = new CalculatedTotals();
        
        BigDecimal totalHT = BigDecimal.ZERO;
        BigDecimal totalTVA = BigDecimal.ZERO;
        
        if (invoice.getLines() != null) {
            for (InvoiceLineDTO line : invoice.getLines()) {
                // Montant HT = Prix Unitaire × Quantité
                BigDecimal unitPrice = safe(line.getUnitPrice());
                BigDecimal quantity = safe(line.getQuantity());
                BigDecimal discount = safe(line.getDiscount());
                BigDecimal vatRate = safe(line.getVatRate());
                
                // Calcul HT de la ligne
                BigDecimal lineHT = unitPrice.multiply(quantity);
                
                // Appliquer remise si présente (en valeur, pas en pourcentage)
                lineHT = lineHT.subtract(discount);
                
                totalHT = totalHT.add(lineHT);
                
                // TVA sur le HT
                if (vatRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal lineTVA = lineHT.multiply(vatRate)
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                    totalTVA = totalTVA.add(lineTVA);
                }
            }
        }
        
        // Total TTC = HT + TVA
        BigDecimal totalTTC = totalHT.add(totalTVA);
        
        // Timbre de quittance : 1000 FCFA si paiement cash
        BigDecimal stampTax = BigDecimal.ZERO;
        String paymentMode = invoice.getPaymentMode();
        if (paymentMode != null && paymentMode.equalsIgnoreCase("CASH")) {
            stampTax = BigDecimal.valueOf(1000);
        }
        
        // Total à payer = TTC + Timbre
        BigDecimal totalToPay = totalTTC.add(stampTax);
        
        result.totalHT = totalHT;
        result.totalTVA = totalTVA;
        result.totalTTC = totalTTC;
        result.otherTaxes = BigDecimal.ZERO; // Pas d'autres taxes pour l'instant
        result.stampTax = stampTax;
        result.totalToPay = totalToPay;
        
        return result;
    }

    /**
     * Construit les lignes du tableau produits.
     * Montant HT = Prix Unitaire × Quantité (SANS TVA)
     */
    private String buildInvoiceLines(List<InvoiceLineDTO> lines) {
        if (lines == null || lines.isEmpty()) {
            return "<tr><td colspan=\"8\" style=\"text-align:center;\">Aucun article</td></tr>"; 
        }
        
        StringBuilder sb = new StringBuilder();
        int index = 1;
        
        for (InvoiceLineDTO line : lines) {
            sb.append("<tr>");
            
            // Référence
            String ref = line.getProductCode() != null ? line.getProductCode() : 
                    String.format("ART-%03d", index);
            sb.append("<td>").append(escapeHtml(ref)).append("</td>");
            
            // Désignation
            sb.append("<td class=\"left\">").append(escapeHtml(nullSafe(line.getDescription()))).append("</td>");
            
            // Prix Unitaire HT
            sb.append("<td class=\"right\">").append(formatAmount(line.getUnitPrice())).append("</td>");
            
            // Quantité
            sb.append("<td>").append(formatQuantity(line.getQuantity())).append("</td>");
            
            // Unité
            sb.append("<td>").append(escapeHtml(nullSafe(line.getUnit(), "pcs"))).append("</td>");
            
            // Taxes (%)
            sb.append("<td>").append(escapeHtml(formatTaxDisplay(line.getVatRate()))).append("</td>");
            
            // Remise
            sb.append("<td>").append(formatDiscount(line.getDiscount())).append("</td>");
            
            // Montant HT = Prix Unitaire × Quantité (SANS TVA)
            BigDecimal montantHT = safe(line.getUnitPrice())
                    .multiply(safe(line.getQuantity()))
                    .subtract(safe(line.getDiscount()));
            sb.append("<td class=\"right\">").append(formatAmount(montantHT)).append("</td>");
            
            sb.append("</tr>");
            index++;
        }
        
        return sb.toString();
    }

    /**
     * Construit le résumé TVA par catégorie.
     */
    private String buildTaxSummary(List<InvoiceLineDTO> lines, CalculatedTotals totals) {
        Map<BigDecimal, TaxSummaryItem> taxSummaries = new LinkedHashMap<>();
        
        if (lines != null) {
            for (InvoiceLineDTO line : lines) {
                BigDecimal vatRate = safe(line.getVatRate());
                BigDecimal unitPrice = safe(line.getUnitPrice());
                BigDecimal quantity = safe(line.getQuantity());
                BigDecimal discount = safe(line.getDiscount());
                
                // Montant HT de la ligne
                BigDecimal lineHT = unitPrice.multiply(quantity).subtract(discount);
                
                // TVA de la ligne
                BigDecimal lineTVA = BigDecimal.ZERO;
                if (vatRate.compareTo(BigDecimal.ZERO) > 0) {
                    lineTVA = lineHT.multiply(vatRate)
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                }
                
                taxSummaries.computeIfAbsent(vatRate, TaxSummaryItem::new)
                        .addAmount(lineHT, lineTVA);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (taxSummaries.isEmpty()) {
            sb.append("<tr><td colspan=\"4\" style=\"text-align:center;\">-</td></tr>");
        } else {
            for (TaxSummaryItem summary : taxSummaries.values()) {
                sb.append("<tr>");
                sb.append("<td class=\"left\">").append(escapeHtml(summary.getCategoryName())).append("</td>");
                sb.append("<td class=\"right\">").append(formatAmount(summary.getSubtotal())).append("</td>");
                sb.append("<td>").append(formatPercentage(summary.getRate())).append("</td>");
                sb.append("<td class=\"right\">").append(formatAmount(summary.getTotalTax())).append("</td>");
                sb.append("</tr>");
            }
        }
        
        return sb.toString();
    }

    private byte[] convertHtmlToPdf(String html) throws DocumentException, IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        renderer.createPDF(outputStream);
        
        return outputStream.toByteArray();
    }

    // ==================== UTILITAIRES ====================

    private String encodeResourceToBase64(Resource resource, String mimeType) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:" + mimeType + ";base64," + base64;
        }
    }

    private String getSellerLogoBase64(String taxId) {
        if (taxId == null || taxId.isEmpty()) {
            return defaultLogoBase64;
        }
        
        try {
            Path logoPath = Path.of(sellerLogosPath, taxId + ".png");
            if (Files.exists(logoPath)) {
                byte[] bytes = Files.readAllBytes(logoPath);
                String base64 = Base64.getEncoder().encodeToString(bytes);
                return "data:image/png;base64," + base64;
            }
        } catch (IOException e) {
            log.warn("Impossible de charger le logo pour {}: {}", taxId, e.getMessage());
        }
        
        return defaultLogoBase64;
    }

    private String formatBase64Image(String base64, String mimeType) {
        if (base64 == null || base64.isEmpty()) {
            return createPlaceholderImage();
        }
        if (base64.startsWith("data:")) {
            return base64;
        }
        return "data:" + mimeType + ";base64," + base64;
    }

    private String createPlaceholderImage() {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    private String formatDateTime(Instant instant) {
        if (instant == null) return "-";
        return DATE_TIME_FORMATTER.format(instant);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0";
        return AMOUNT_FORMAT.format(amount.setScale(0, RoundingMode.HALF_UP));
    }

    private String formatQuantity(BigDecimal quantity) {
        if (quantity == null) return "1";
        if (quantity.stripTrailingZeros().scale() <= 0) {
            return quantity.toBigInteger().toString();
        }
        return quantity.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private String formatTaxDisplay(BigDecimal vatRate) {
        if (vatRate == null || vatRate.compareTo(BigDecimal.ZERO) == 0) {
            return "EXONÉRÉ";
        }
        return "TVA (" + vatRate.setScale(0, RoundingMode.HALF_UP).toString() + ")";
    }

    private String formatDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) return "0";
        return formatAmount(discount);
    }

    private String formatPercentage(BigDecimal rate) {
        if (rate == null) return "0%";
        return rate.setScale(0, RoundingMode.HALF_UP).toString() + "%";
    }

    private String translatePaymentMode(String paymentMode) {
        if (paymentMode == null) return "Non spécifié";
        return switch (paymentMode.toUpperCase()) {
            case "CASH" -> "Cash";
            case "CARD", "CREDIT_CARD" -> "Carte bancaire";
            case "TRANSFER", "BANK_TRANSFER" -> "Virement bancaire";
            case "CHECK", "CHEQUE" -> "Chèque";
            case "MOBILE_MONEY", "MOBILE-MONEY" -> "Mobile Money";
            case "CREDIT" -> "Crédit";
            default -> paymentMode;
        };
    }

    private String getSellerRegime(SellerDTO seller) {
        if (seller != null && seller.getRegime() != null && !seller.getRegime().isBlank()) {
            return seller.getRegime();
        }
        return defaultRegime;
    }

    private String getSellerTaxCenter(SellerDTO seller) {
        if (seller != null && seller.getTaxCenter() != null && !seller.getTaxCenter().isBlank()) {
            return seller.getTaxCenter();
        }
        return defaultTaxCenter;
    }

    private String nullSafe(String value) {
        return value != null && !value.isBlank() ? value : "";
    }

    private String nullSafe(String value, String defaultValue) {
        return value != null && !value.isBlank() ? value : (defaultValue != null ? defaultValue : "");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * Template HTML par défaut si le fichier n'est pas trouvé.
     */
    private String getDefaultTemplate() {
        return """
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr">
<head>
    <meta charset="UTF-8"/>
    <title>Facture {{FNE_REFERENCE}}</title>
    <style type="text/css">
        body { font-family: Arial, sans-serif; font-size: 10pt; margin: 20px; }
        table { width: 100%; border-collapse: collapse; margin: 10px 0; }
        th, td { border: 1px solid #000; padding: 5px; text-align: left; }
        th { background-color: #f0f0f0; }
        .right { text-align: right; }
        .left { text-align: left; }
        h1 { font-size: 14pt; text-align: center; }
        .totals { width: 50%; margin-left: auto; }
    </style>
</head>
<body>
    <h1>FACTURE NORMALISÉE ÉLECTRONIQUE</h1>
    <p><strong>{{SELLER_COMPANY_NAME}}</strong></p>
    <p>NCC: {{SELLER_NCC}} | Adresse: {{SELLER_ADDRESS}}</p>
    <p>Référence: {{FNE_REFERENCE}} | Date: {{INVOICE_DATE_TIME}}</p>
    <p><strong>Client:</strong> {{BUYER_NAME}} - {{BUYER_ADDRESS}}</p>
    
    <table>
        <thead>
            <tr><th>Réf</th><th>Désignation</th><th>P.U HT</th><th>Qté</th><th>Unité</th><th>TVA</th><th>Rem.</th><th>Montant HT</th></tr>
        </thead>
        <tbody>{{INVOICE_LINES}}</tbody>
    </table>
    
    <table class="totals">
        <tr><td>TOTAL HT</td><td class="right">{{TOTAL_HT}}</td></tr>
        <tr><td>TVA</td><td class="right">{{TOTAL_TVA}}</td></tr>
        <tr><td>TOTAL TTC</td><td class="right">{{TOTAL_TTC}}</td></tr>
        <tr><td>AUTRES TAXES</td><td class="right">{{OTHER_TAXES}}</td></tr>
        <tr><td>TIMBRE</td><td class="right">{{STAMP_TAX}}</td></tr>
        <tr><td><strong>TOTAL A PAYER</strong></td><td class="right"><strong>{{TOTAL_TO_PAY}}</strong></td></tr>
    </table>
    
    <div style="text-align:center; margin-top:20px;">
        <img src="{{QR_CODE_BASE64}}" width="100" height="100"/>
    </div>
    
    <h3>RESUME DE LA FACTURE</h3>
    <table>
        <thead><tr><th>CATEGORIE</th><th>SOUS-TOTAL</th><th>TAUX (%)</th><th>TOTAL TAXES</th></tr></thead>
        <tbody>{{TAX_SUMMARY_LINES}}</tbody>
    </table>
</body>
</html>
""";
    }

    // ==================== CLASSES INTERNES ====================

    public static class PdfGenerationException extends RuntimeException {
        public PdfGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Totaux calculés comme FNE.
     */
    private static class CalculatedTotals {
        BigDecimal totalHT = BigDecimal.ZERO;
        BigDecimal totalTVA = BigDecimal.ZERO;
        BigDecimal totalTTC = BigDecimal.ZERO;
        BigDecimal otherTaxes = BigDecimal.ZERO;
        BigDecimal stampTax = BigDecimal.ZERO;
        BigDecimal totalToPay = BigDecimal.ZERO;
    }

    /**
     * Item de résumé TVA par catégorie.
     */
    private static class TaxSummaryItem {
        private final BigDecimal rate;
        private BigDecimal subtotal = BigDecimal.ZERO;
        private BigDecimal totalTax = BigDecimal.ZERO;

        public TaxSummaryItem(BigDecimal rate) {
            this.rate = rate != null ? rate : BigDecimal.ZERO;
        }

        public void addAmount(BigDecimal amount, BigDecimal taxAmount) {
            this.subtotal = this.subtotal.add(amount != null ? amount : BigDecimal.ZERO);
            this.totalTax = this.totalTax.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        }

        public String getCategoryName() {
            if (rate.compareTo(BigDecimal.ZERO) == 0) return "EXONÉRÉ";
            return "TVA normal - TVA sur HT " + rate.setScale(2, RoundingMode.HALF_UP) + "% - A";
        }

        public BigDecimal getRate() { return rate; }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getTotalTax() { return totalTax; }
    }
}