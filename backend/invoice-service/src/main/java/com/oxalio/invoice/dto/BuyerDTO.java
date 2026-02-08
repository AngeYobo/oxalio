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
public class BuyerDTO {

    // ✅ taxId OPTIONNEL pour BAPA (producteurs individuels sans NCC)
    @Pattern(regexp = "^((CI)?[0-9]{7}[A-Z])?$", message = "Format NCC invalide (optionnel pour BAPA)")
    private String taxId;

    @NotBlank(message = "Le nom de l'acheteur est obligatoire")
    private String name;

    private String address;
    private String email;
    private String phone;
    
}
