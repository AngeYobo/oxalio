package com.oxalio.invoice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identification
    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true, length = 20)
    private String ncc;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    // FNE Configuration
    @Column(length = 500)
    private String fneApiKey;

    @Column(length = 255)
    @Builder.Default
    private String fneEstablishment = "Siège";

    @Column(length = 255)
    @Builder.Default
    private String fnePointOfSale = "Caisse 1";

    // Subscription
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String subscriptionPlan = "starter";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String subscriptionStatus = "trial";

    private LocalDateTime subscriptionStartedAt;
    private LocalDateTime subscriptionEndsAt;

    // Limites
    @Builder.Default
    private Integer monthlyInvoiceLimit = 50;

    @Builder.Default
    private Integer monthlyInvoiceCount = 0;

    // Contact
    @Column(nullable = false, unique = true)
    private String ownerEmail;

    @Column(nullable = false)
    private String ownerName;

    private String ownerPhone;

    // Logo
    private String logoUrl;

    // Statut
    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isVerified = false;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
