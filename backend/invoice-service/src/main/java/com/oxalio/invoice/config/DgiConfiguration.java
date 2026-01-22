package com.oxalio.invoice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour l'intégration DGI FNE.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dgi")
public class DgiConfiguration {

    /**
     * Active ou désactive l'intégration DGI.
     */
    private boolean enabled = true;

    /**
     * Utilise le client mock (true) ou le client réel (false).
     */
    private boolean mock = true;

    /**
     * URL de base de l'API DGI (ancienne config - gardée pour compatibilité).
     */
    private String baseUrl = "https://fne.dgi.gouv.ci/api";

    /**
     * Timeout pour les appels API (en millisecondes).
     */
    private int timeout = 30000;

    /**
     * Token d'authentification (ancienne config - gardée pour compatibilité).
     */
    private String authToken;

    /**
     * Configuration des tentatives de retry.
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * Configuration de l'authentification.
     */
    private AuthConfig auth = new AuthConfig();

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long backoffDelay = 1000;
        
        // Nouveaux champs pour FNE
        private long initialInterval = 2000;
        private double multiplier = 2.0;
        private long maxInterval = 10000;
    }

    @Data
    public static class AuthConfig {
        private String clientId;
        private String clientSecret;
        private String tokenUrl;
    }
}

// /**
//  * ════════════════════════════════════════════════════════════════
//  * NOUVELLE CONFIGURATION SPÉCIFIQUE FNE
//  * ════════════════════════════════════════════════════════════════
//  */
// @Data
// @Configuration
// @ConfigurationProperties(prefix = "fne")
// public class FneConfiguration {
    
//     private ApiConfig api = new ApiConfig();
//     private AuthConfig auth = new AuthConfig();
//     private EstablishmentConfig establishment = new EstablishmentConfig();
//     private CompanyConfig company = new CompanyConfig();
//     private RetryConfig retry = new RetryConfig();
    
//     @Data
//     public static class ApiConfig {
//         private String baseUrl;
//         private int timeout = 30000;
//     }
    
//     @Data
//     public static class AuthConfig {
//         private String apiKey;
//     }
    
//     @Data
//     public static class EstablishmentConfig {
//         private String name;
//         private String pointOfSale;
//     }
    
//     @Data
//     public static class CompanyConfig {
//         private String ncc;
//         private String name;
//         private String email;
//         private String phone;
//         private String address;
//         private String taxRegime;
//     }
    
//     @Data
//     public static class RetryConfig {
//         private int maxAttempts = 3;
//         private long initialInterval = 2000;
//         private double multiplier = 2.0;
//         private long maxInterval = 10000;
//     }
// }