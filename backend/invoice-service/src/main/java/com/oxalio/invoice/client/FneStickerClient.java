package com.oxalio.invoice.client;

import com.oxalio.invoice.config.FneConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Client pour l'API FNE (Facture Normalisée Électronique).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FneStickerClient {

    private final RestTemplate restTemplate;
    private final FneConfiguration config;

    /**
     * Signer une facture (vente ou achat).
     */
    @Retryable(
        value = {HttpServerErrorException.class},
        maxAttemptsExpression = "#{@fneConfiguration.retry.maxAttempts}",
        backoff = @Backoff(
            delayExpression = "#{@fneConfiguration.retry.initialInterval}",
            multiplierExpression = "#{@fneConfiguration.retry.multiplier}",
            maxDelayExpression = "#{@fneConfiguration.retry.maxInterval}"
        )
    )
    public FneInvoiceResponse signInvoice(FneInvoiceRequest request) {
        log.info("Signing {} invoice for client: {}", 
            request.getInvoiceType(), request.getClientCompanyName());
        
        String url = config.getApi().getBaseUrl() + "/external/invoices/sign";
        
        HttpHeaders headers = createHeaders();
        HttpEntity<FneInvoiceRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FneInvoiceResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                FneInvoiceResponse.class
            );
            
            FneInvoiceResponse body = response.getBody();
            log.info("Invoice signed successfully. Reference: {}, Balance: {}", 
                body.getReference(), body.getBalanceFunds());
            
            return body;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error signing invoice: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FneApiException("Error signing invoice: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("Server error signing invoice: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FneApiException("FNE server error: " + e.getMessage(), e);
        }
    }

    /**
     * Créer un avoir (refund).
     */
    @Retryable(
        value = {HttpServerErrorException.class},
        maxAttemptsExpression = "#{@fneConfiguration.retry.maxAttempts}",
        backoff = @Backoff(
            delayExpression = "#{@fneConfiguration.retry.initialInterval}",
            multiplierExpression = "#{@fneConfiguration.retry.multiplier}",
            maxDelayExpression = "#{@fneConfiguration.retry.maxInterval}"
        )
    )
    public FneRefundResponse createRefund(String invoiceId, FneRefundRequest request) {
        log.info("Creating refund for invoice: {}", invoiceId);
        
        String url = String.format("%s/external/invoices/%s/refund", 
            config.getApi().getBaseUrl(), invoiceId);
        
        HttpHeaders headers = createHeaders();
        HttpEntity<FneRefundRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FneRefundResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                FneRefundResponse.class
            );
            
            FneRefundResponse body = response.getBody();
            log.info("Refund created successfully. Reference: {}", body.getReference());
            
            return body;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error creating refund: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FneApiException("Error creating refund: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("Server error creating refund: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FneApiException("FNE server error: " + e.getMessage(), e);
        }
    }

    /**
     * Créer les headers HTTP avec le Bearer token.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(config.getAuth().getApiKey());
        return headers;
    }

    // ════════════════════════════════════════════════════════════════
    // DTOs
    // ════════════════════════════════════════════════════════════════

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FneInvoiceRequest {
        private String invoiceType;      // "sale" ou "purchase"
        private String paymentMethod;    // "cash", "mobile-money", etc.
        private String template;         // "B2B", "B2C", "B2F"
        private Boolean isRne = false;
        
        // Client/Fournisseur
        private String clientNcc;
        private String clientCompanyName;
        private String clientPhone;
        private String clientEmail;
        
        // Etablissement
        private String establishment;
        private String pointOfSale;
        
        // International (B2F)
        private String foreignCurrency;
        private BigDecimal foreignCurrencyRate;
        
        // Message commercial
        private String commercialMessage;
        
        // Articles
        private List<FneInvoiceItem> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FneInvoiceItem {
        private String reference;
        private List<String> taxes;          // ["TVA"], ["TVAB"], []
        private String description;
        private Integer quantity;
        private BigDecimal amount;           // Prix unitaire HT
        private Integer discount;            // % de remise (0-100)
        private String measurementUnit;      // "kg", "L", "pcs", etc.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FneRefundRequest {
        private List<RefundItem> items;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RefundItem {
            private String id;               // ID de l'article original
            private Integer quantity;        // Quantité à rembourser
        }
    }

    @Data
    public static class FneInvoiceResponse {
        private String ncc;
        private String reference;
        private String token;
        private Boolean warning;
        private Integer balanceFunds;
        private InvoiceDetails invoice;
        
        @Data
        public static class InvoiceDetails {
            private String id;
            private String reference;
            private String type;
            private String status;
            private LocalDateTime date;
            private BigDecimal amount;
            private BigDecimal totalTaxes;
            private List<InvoiceItemDetails> items;
            
            @Data
            public static class InvoiceItemDetails {
                private String id;
                private String description;
                private Integer quantity;
                private BigDecimal amount;
            }
        }
    }

    @Data
    public static class FneRefundResponse {
        private String ncc;
        private String reference;
        private String token;
        private Integer balanceFunds;
    }

    /**
     * Exception personnalisée FNE.
     */
    public static class FneApiException extends RuntimeException {
        public FneApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}