package com.oxalio.invoice.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(
    origins = {
        "https://3000-sidereal-election-ozhudl.us1.demeter.run",
        "http://localhost:3000"
    },
    allowCredentials = "true"
)
@RestController
@RequestMapping(value = "/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
public class InvoiceController {

    // ✅ Source mock commune
    private Map<String, Object> mockInvoice(String number, String buyer, int amount, String status) {
        Map<String, Object> inv = new HashMap<>();
        inv.put("invoiceNumber", number);
        inv.put("currency", "XOF");
        inv.put("status", status);
        inv.put("buyer", Map.of("name", buyer));
        inv.put("totals", Map.of("totalAmount", amount));
        return inv;
    }

    // ✅ Liste pour le tableau: GET /api/v1/invoices
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listInvoices() {
        List<Map<String, Object>> invoices = new ArrayList<>();
        invoices.add(mockInvoice("INV-2025-0001", "Client Démo", 11800, "MOCK_READY"));
        invoices.add(mockInvoice("INV-2025-0002", "Client Test",  5600,  "MOCK_SENT"));
        invoices.add(mockInvoice("INV-2025-0003", "Client VIP",   22500, "MOCK_READY"));
        return ResponseEntity.ok(invoices);
    }

    // ✅ Détail unitaire: GET /api/v1/invoices/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInvoiceById(@PathVariable String id) {
        // Pour la démo: renvoie toujours un mock en réutilisant l'id
        Map<String, Object> inv = mockInvoice(id, "Client Démo", 11800, "MOCK_READY");
        return ResponseEntity.ok(inv);
    }

    // ✅ Exemple unitaire existant: GET /api/v1/invoices/demo
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demoInvoice() {
        return ResponseEntity.ok(mockInvoice("INV-2025-0001", "Client Démo", 11800, "MOCK_READY"));
    }

    // ✅ Création mock: POST /api/v1/invoices
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody Map<String, Object> invoice) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "RECEIVED");
        response.put("invoiceNumber", invoice.get("invoiceNumber"));
        response.put("message", "Facture mock reçue côté invoice-service ✅");
        return ResponseEntity.ok(response);
    }
}
