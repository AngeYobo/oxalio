package com.oxalio.invoice.client;

import com.oxalio.invoice.dto.DgiInvoiceRequestDTO;
import com.oxalio.invoice.dto.DgiInvoiceResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import jakarta.annotation.PostConstruct;

/**
 * Client DGI — envoi des factures vers l’API FNE (mock, sandbox ou prod selon profil).
 */
@Service
public class DgiClient {

    private static final Logger log = LoggerFactory.getLogger(DgiClient.class);

    private final RestTemplate restTemplate;

    @Value("${dgi.endpoint}")
    private String dgiEndpoint;

    @Value("${dgi.auth-token:}")
    private String dgiAuthToken;

    @Value("${dgi.timeout-ms:10000}")
    private int timeoutMs;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public DgiClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    @PostConstruct
    public void init() {
        log.info("[DGI CLIENT] Profil actif = {}", activeProfile);
        log.info("[DGI CLIENT] Endpoint configuré = {}", dgiEndpoint);
        log.info("[DGI CLIENT] Timeout configuré = {} ms", timeoutMs);
    }

    /**
     * Envoie une facture formatée au service FNE/DGI et récupère la réponse.
     */
    public DgiInvoiceResponseDTO sendInvoice(DgiInvoiceRequestDTO request) {
        try {
            log.info("[DGI] Envoi facture {} à {}", request.getInvoiceNumber(), dgiEndpoint);

            // Reconfigurer les timeouts dynamiquement à chaque envoi
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeoutMs);
            factory.setReadTimeout(timeoutMs);
            restTemplate.setRequestFactory(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (dgiAuthToken != null && !dgiAuthToken.isBlank()) {
                headers.setBearerAuth(dgiAuthToken);
            }

            HttpEntity<DgiInvoiceRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<DgiInvoiceResponseDTO> response = restTemplate.exchange(
                    dgiEndpoint,
                    HttpMethod.POST,
                    entity,
                    DgiInvoiceResponseDTO.class
            );

            log.info("[DGI] Réponse pour facture {} : HTTP {}",
                    request.getInvoiceNumber(), response.getStatusCode());

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            log.error("[DGI] Erreur HTTP {} — body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            DgiInvoiceResponseDTO error = new DgiInvoiceResponseDTO();
            error.setStatus("REJECTED");
            error.setMessage(String.format("Erreur HTTP %s : %s",
                    ex.getStatusCode(), ex.getResponseBodyAsString()));
            return error;

        } catch (ResourceAccessException ex) {
            // Typiquement timeout ou DNS
            log.error("[DGI] Timeout ou erreur réseau: {}", ex.getMessage());
            DgiInvoiceResponseDTO error = new DgiInvoiceResponseDTO();
            error.setStatus("REJECTED");
            error.setMessage(String.format("Erreur réseau (%s): %s",
                    activeProfile, ex.getMessage()));
            return error;

        } catch (Exception ex) {
            log.error("[DGI] Exception interne: {}", ex.getMessage(), ex);
            DgiInvoiceResponseDTO error = new DgiInvoiceResponseDTO();
            error.setStatus("REJECTED");
            error.setMessage("Exception interne: " + ex.getMessage());
            return error;
        }
    }
}
