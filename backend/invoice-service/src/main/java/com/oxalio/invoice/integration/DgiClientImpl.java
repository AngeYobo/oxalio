package com.oxalio.invoice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxalio.invoice.config.DgiConfiguration;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client réel pour l'intégration avec l'API DGI FNE.
 * Activé uniquement si dgi.mock=false dans application.yml
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "dgi.mock", havingValue = "false")
@RequiredArgsConstructor
public class DgiClientImpl {

    private final RestTemplate restTemplate;
    private final DgiConfiguration dgiConfig;
    private final ObjectMapper objectMapper;

    @Value("${fne.auth.api-key:}")
    private String apiKey() {
        String key = fneConfiguration.getApiKey();
        if (key == null || key.isBlank()) {
        throw new IllegalStateException("Missing fne.auth.api-key (set env var FNE_AUTH_API_KEY)");
        }
        return key;
    }
    }

    public DgiClientMock.DgiCertification submitInvoice(InvoiceEntity invoice) {
        log.info("📤 [DGI REAL] Soumission de la facture à l'API DGI FNE");

        try {
            Map<String, Object> dgiPayload = buildDgiPayload(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(DGI_BEARER_TOKEN);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(dgiPayload, headers);

            String url = dgiConfig.getBaseUrl() + "/ws/external/invoices/sign";

            log.debug("📡 URL DGI: {}", url);
            log.debug("📦 Payload: {}", objectMapper.writeValueAsString(dgiPayload));

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                log.info("✅ [DGI REAL] Facture acceptée par la DGI");
                
                return parseDgiResponse(body, "INV-" + System.currentTimeMillis());
            } else {
                log.error("❌ [DGI REAL] Erreur HTTP {} lors de la soumission", response.getStatusCode());
                throw new RuntimeException("Erreur DGI: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ [DGI REAL] Erreur lors de la soumission à la DGI", e);
            throw new RuntimeException("Échec de la soumission à la DGI: " + e.getMessage(), e);
        }
    }

    public DgiClientMock.DgiCertification simulateCertification(InvoiceEntity invoice) {
        return submitInvoice(invoice);
    }

    private Map<String, Object> buildDgiPayload(InvoiceEntity invoice) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("invoiceType", mapInvoiceType(invoice.getInvoiceType()));
        payload.put("paymentMethod", mapPaymentMethod(invoice.getPaymentMode()));
        payload.put("template", "B2B");
        payload.put("isRne", false);
        payload.put("clientNcc", invoice.getBuyerTaxId());
        payload.put("clientCompanyName", invoice.getBuyerName());
        payload.put("clientPhone", "0700000000");
        payload.put("clientEmail", "client@default.ci");
        payload.put("establishment", invoice.getSellerCompanyName());
        payload.put("pointOfSale", "Point de Vente Principal");
        payload.put("items", buildItems(invoice));
        
        return payload;
    }

    private List<Map<String, Object>> buildItems(InvoiceEntity invoice) {
        try {
            List<InvoiceLineEntity> lines = invoice.getLines();
            
            if (lines == null || lines.isEmpty()) {
                Map<String, Object> defaultItem = new HashMap<>();
                defaultItem.put("taxes", List.of("TVA"));
                defaultItem.put("description", "Article par défaut");
                defaultItem.put("quantity", 1);
                defaultItem.put("amount", 10000);
                return List.of(defaultItem);
            }

            return lines.stream()
                .map(line -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("taxes", List.of("TVA"));
                    item.put("description", line.getDescription());
                    item.put("quantity", line.getQuantity().intValue());
                    item.put("amount", line.getUnitPrice().multiply(line.getQuantity()).intValue());
                    return item;
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur lors de la construction des items", e);
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("taxes", List.of("TVA"));
            defaultItem.put("description", "Article");
            defaultItem.put("quantity", 1);
            defaultItem.put("amount", 10000);
            return List.of(defaultItem);
        }
    }

    private String mapInvoiceType(String invoiceType) {
        if (invoiceType == null) return "sale";
        
        return switch (invoiceType) {
            case "STANDARD" -> "sale";
            case "PROFORMA" -> "proforma";
            case "CREDIT_NOTE" -> "credit-note";
            default -> "sale";
        };
    }

    private String mapPaymentMethod(String paymentMode) {
        if (paymentMode == null) return "mobile-money";
        
        return switch (paymentMode) {
            case "CASH" -> "cash";
            case "TRANSFER" -> "bank-transfer";
            case "CARD" -> "credit-card";
            case "MOBILE" -> "mobile-money";
            default -> "mobile-money";
        };
    }

    private DgiClientMock.DgiCertification parseDgiResponse(Map<String, Object> response, String invoiceNumber) {
        String dgiReference = (String) response.getOrDefault("invoiceId", "DGI-REF-UNKNOWN");
        String qrCode = (String) response.getOrDefault("qrCode", "");
        String stickerId = (String) response.getOrDefault("stickerId", "STKR-UNKNOWN");

        return DgiClientMock.DgiCertification.builder()
            .certificationId(dgiReference)
            .invoiceNumber(invoiceNumber)
            .dgiReference(dgiReference)
            .stickerId(stickerId)
            .certifiedAt(Instant.now())
            .qrCodeData(qrCode)
            .qrBase64(qrCode)
            .status("CERTIFIED")
            .message("Facture certifiée par la DGI (API Réelle)")
            .build();
    }
}