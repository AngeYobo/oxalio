package com.oxalio.invoice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantRequest {
    
    @Size(min = 2, max = 255)
    private String companyName;
    
    @Email
    private String ownerEmail;
    
    private String ownerName;
    
    @Pattern(regexp = "^0[0-9]{9}$", message = "Numéro de téléphone ivoirien invalide")
    private String ownerPhone;
    
    private String fneApiKey;
    private String fneEstablishment;
    private String fnePointOfSale;
    
    private String logoUrl;
    
    private Boolean isActive;
}