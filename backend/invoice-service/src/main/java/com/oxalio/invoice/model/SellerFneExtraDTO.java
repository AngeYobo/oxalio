package com.oxalio.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerFneExtraDTO {
    private String sellerDisplayName;   // Nom du vendeur (ex : Gestionnaire principal SAMSON)
    private String pointOfSaleName;     // Nom de PDV (SIEGE)
}
