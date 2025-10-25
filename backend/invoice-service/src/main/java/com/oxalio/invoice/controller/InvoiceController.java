package com.oxalio.invoice.controller;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Validated
@Tag(name = "Invoices", description = "API de gestion des factures FNE")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(
        summary = "Créer une facture",
        description = "Crée une nouvelle facture conforme DGI avec génération automatique du numéro, stickerId et QR code",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Facture créée avec succès",
                content = @Content(schema = @Schema(implementation = InvoiceResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Données de facture invalides"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
        }
    )
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        log.info("Création d'une facture de type {} pour le vendeur {}",
                request.getInvoiceType(), request.getSeller().getTaxId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        log.info("Facture créée avec succès : {}", response.getInvoiceNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les factures", description = "Récupère la liste complète des factures enregistrées")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        log.info("Récupération de toutes les factures");
        List<InvoiceResponse> invoices = invoiceService.getAllInvoices();
        log.info("Nombre de factures récupérées : {}", invoices.size());
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une facture par ID", description = "Récupère les détails d'une facture spécifique")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        log.info("Récupération de la facture avec ID : {}", id);
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Récupérer une facture par numéro", description = "Récupère les détails d'une facture par son numéro unique")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        log.info("Récupération de la facture numéro : {}", invoiceNumber);
        InvoiceResponse response = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une facture", description = "Met à jour les données d'une facture existante")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        log.info("Mise à jour de la facture avec ID : {}", id);
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        log.info("Facture mise à jour avec succès : {}", response.getInvoiceNumber());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une facture", description = "Supprime une facture du système")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        log.info("Suppression de la facture avec ID : {}", id);
        invoiceService.deleteInvoice(id);
        log.info("Facture supprimée avec succès");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit-to-dgi")
    @Operation(summary = "Soumettre une facture à la DGI", description = "Soumet une facture au système FNE de la DGI et récupère la référence")
    public ResponseEntity<InvoiceResponse> submitToDgi(@PathVariable Long id) {
        log.info("Soumission de la facture {} à la DGI", id);
        InvoiceResponse response = invoiceService.submitToDgi(id);
        log.info("Facture soumise avec succès. Référence DGI : {}", response.getDgiReference());
        return ResponseEntity.ok(response);
    }
}
