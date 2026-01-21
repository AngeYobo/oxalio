package com.oxalio.invoice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {
    
    private Long id;
    
    // Identification
    private String companyName;
    private String ncc;
    private String slug;
    
    // FNE Configuration
    private String fneEstablishment;
    private String fnePointOfSale;
    
    // Subscription
    private String subscriptionPlan;
    private String subscriptionStatus;
    private LocalDateTime subscriptionStartedAt;
    private LocalDateTime subscriptionEndsAt;
    
    // Limites
    private Integer monthlyInvoiceLimit;
    private Integer monthlyInvoiceCount;
    
    // Contact
    private String ownerEmail;
    private String ownerName;
    private String ownerPhone;
    
    // Logo
    private String logoUrl;
    
    // Statut
    private Boolean isActive;
    private Boolean isVerified;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}