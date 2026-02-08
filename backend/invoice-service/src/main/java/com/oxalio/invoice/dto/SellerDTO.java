package com.oxalio.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(CI)?[0-9]{7}[A-Z]$", message = "Format NCC invalide (ex: 2505842N ou CI2505842N)")
    private String taxId;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String companyName;

    private String address;
    private String email;
    private String phone;
    private String pointOfSaleName;
    private String sellerDisplayName;
}
