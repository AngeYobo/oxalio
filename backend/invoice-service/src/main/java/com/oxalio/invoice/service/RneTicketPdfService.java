package com.oxalio.invoice.service;

import com.oxalio.invoice.entity.InvoiceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service de génération de PDF pour les tickets RNE (Reçu Normalisé Électronique)
 * 
 * Ce service génère un PDF conforme aux spécifications FNE/DGI pour les tickets RNE.
 * Le ticket contient :
 * - Logo FNE
 * - QR Code de vérification
 * - Référence FNE
 * - Informations vendeur/client
 * - Détails de la transaction
 * - Mode de paiement
 * 
 * TODO: Implémenter la génération PDF réelle avec PDFBox (étape 2.3)
 */
@Slf4j
@Service
public class RneTicketPdfService {

    /**
     * Génère un PDF de ticket RNE
     * 
     * @param invoice L'entité facture contenant les données FNE
     * @param qrPng Le QR code en format PNG (bytes)
     * @return PDF en bytes
     */
    public byte[] render(InvoiceEntity invoice, byte[] qrPng) {
        log.warn("⚠️ RneTicketPdfService.render() appelé avec stub - PDF vide retourné");
        log.debug("Invoice ID: {}, FNE Reference: {}, QR size: {} bytes", 
            invoice.getId(), 
            invoice.getFneReference(), 
            qrPng != null ? qrPng.length : 0
        );
        
        // TODO: Implémenter la génération PDF réelle avec PDFBox
        // 
        // Étapes à implémenter :
        // 1. Créer document PDF (A4 ou format ticket thermique)
        // 2. Ajouter le logo FNE en haut
        // 3. Ajouter la référence FNE
        // 4. Insérer le QR code
        // 5. Ajouter les informations vendeur
        // 6. Ajouter les informations client
        // 7. Ajouter les détails de transaction
        // 8. Ajouter le mode de paiement
        // 9. Ajouter le footer avec mentions légales
        // 10. Retourner le PDF en bytes
        
        // Stub temporaire : renvoie un PDF vide minimal (pas conforme)
        return new byte[0];
    }
}