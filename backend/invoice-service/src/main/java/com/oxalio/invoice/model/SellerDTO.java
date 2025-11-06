package com.oxalio.invoice.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDTO {

    @NotBlank(message = "L'identifiant fiscal du vendeur est obligatoire")
    @Pattern(regexp = "^CI[0-9]{7,10}$", message = "Format d'identifiant fiscal invalide")
    private String taxId;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(min = 2, max = 200, message = "Le nom doit contenir entre 2 et 200 caractères")
    private String companyName;

    @NotBlank(message = "L'adresse du vendeur est obligatoire")
    @Size(max = 300, message = "L'adresse ne peut excéder 300 caractères")
    private String address;

    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Format de téléphone invalide")
    private String phone;
}
