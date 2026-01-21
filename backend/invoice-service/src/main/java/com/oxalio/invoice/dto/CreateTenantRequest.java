package com.oxalio.invoice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    
    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(min = 2, max = 255, message = "Le nom doit contenir entre 2 et 255 caractères")
    private String companyName;
    
    @NotBlank(message = "Le NCC est obligatoire")
    @Pattern(regexp = "^[0-9]{7}[A-Z]$", message = "Format NCC invalide (ex: 2505842N)")
    private String ncc;
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String ownerEmail;
    
    @NotBlank(message = "Le nom du propriétaire est obligatoire")
    private String ownerName;
    
    @Pattern(regexp = "^0[0-9]{9}$", message = "Numéro de téléphone ivoirien invalide")
    private String ownerPhone;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
    
    // FNE Configuration (optionnel pour le démarrage)
    private String fneApiKey;
    private String fneEstablishment;
    private String fnePointOfSale;
    
    // Plan d'abonnement (par défaut: starter)
    private String subscriptionPlan;
}