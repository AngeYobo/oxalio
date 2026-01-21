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
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Validated
@Tag(name = "Invoices", description = "API de gestion des factures FNE")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173"
})
public class InvoiceController {

    private final InvoiceService invoiceService;

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    @Operation(
            summary = "Créer une facture",
            description = "Crée une facture FNE complète avec numéro, totals auto, QR et sticker",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Facture créée",
                            content = @Content(schema = @Schema(implementation = InvoiceResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne")
            }
    )
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request
    ) {
        log.info("Création facture type={} IFU vendeur={}",
                request.getInvoiceType(), request.getSeller().getTaxId());

        InvoiceResponse response = invoiceService.createInvoice(request);

        log.info("Facture créée : {}", response.getInvoiceNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // LIST ALL
    // ============================================================
    @GetMapping
    @Operation(summary = "Lister toutes les factures")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> list = invoiceService.getAllInvoices();
        return ResponseEntity.ok(list);
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une facture par ID")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // ============================================================
    // GET BY NUMBER
    // ============================================================
    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Obtenir une facture par numéro")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(
            @PathVariable String invoiceNumber
    ) {
        return ResponseEntity.ok(invoiceService.getInvoiceByNumber(invoiceNumber));
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une facture")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request
    ) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une facture")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // SUBMIT TO DGI (MOCK)
    // ============================================================
    @PostMapping("/{id}/submit-to-dgi")
    @Operation(summary = "Soumettre une facture à la DGI (mock)")
    public ResponseEntity<InvoiceResponse> submitToDgi(@PathVariable Long id) {
        InvoiceResponse resp = invoiceService.submitToDgi(id);
        return ResponseEntity.ok(resp);
    }

    // ============================================================
    // PDF MOCK
    // ============================================================
    @GetMapping("/{id}/pdf")
    @Operation(summary = "Télécharger un PDF mock de facture")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            byte[] pdf = invoiceService.generateFnePdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    "invoice-" + id + ".pdf"
            );

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Erreur génération PDF invoice={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
