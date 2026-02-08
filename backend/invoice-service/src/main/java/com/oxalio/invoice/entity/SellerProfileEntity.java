// src/main/java/com/oxalio/invoice/entity/SellerProfileEntity.java
package com.oxalio.invoice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "seller_profiles",
        indexes = {
                @Index(name = "idx_seller_profiles_tax_id", columnList = "tax_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_id", nullable = false, length = 32, unique = true)
    private String taxId;          // NCC / IFU du vendeur

    @Column(name = "company_name", length = 255)
    private String companyName;    // pour info

    @Column(name = "logo_path", length = 1024)
    private String logoPath;       // chemin absolu ou relatif vers le fichier logo sur disque

    @Column(name = "logo_mime_type", length = 64)
    private String logoMimeType;   // ex: image/png, image/jpeg

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
