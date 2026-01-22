package com.oxalio.invoice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration spécifique FNE (Facture Normalisée Électronique).
 * 
 * Propriétés configurables dans application.yml :
 * <pre>
 * fne:
 *   api:
 *     base-url: http://54.247.95.108/ws
 *     timeout: 30000
 *   auth:
 *     api-key: votre_cle_api
 *   establishment:
 *     name: "Établissement 1"
 *     point-of-sale: "Point de Vente 1"
 *   company:
 *     ncc: "2505842N"
 *     name: "OXALIO SARL"
 *   retry:
 *     max-attempts: 3
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "fne")
public class FneConfiguration {
    
    private ApiConfig api = new ApiConfig();
    private AuthConfig auth = new AuthConfig();
    private EstablishmentConfig establishment = new EstablishmentConfig();
    private CompanyConfig company = new CompanyConfig();
    private RetryConfig retry = new RetryConfig();
    
    @Data
    public static class ApiConfig {
        private String baseUrl;
        private int timeout = 30000;
    }
    
    @Data
    public static class AuthConfig {
        private String apiKey;
    }
    
    @Data
    public static class EstablishmentConfig {
        private String name;
        private String pointOfSale;
    }
    
    @Data
    public static class CompanyConfig {
        private String ncc;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String taxRegime;
    }
    
    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long initialInterval = 2000;
        private double multiplier = 2.0;
        private long maxInterval = 10000;
    }
}