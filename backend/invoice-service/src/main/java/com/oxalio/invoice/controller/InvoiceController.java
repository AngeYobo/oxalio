package com.oxalio.invoice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demoInvoice() {
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("invoiceNumber", "INV-2025-0001");
        invoice.put("currency", "XOF");
        invoice.put("status", "MOCK_READY");

        Map<String, Object> buyer = new HashMap<>();
        buyer.put("name", "Client Démo");
        invoice.put("buyer", buyer);

        Map<String, Object> totals = new HashMap<>();
        totals.put("totalAmount", 11800);
        invoice.put("totals", totals);

        return ResponseEntity.ok(invoice);
    }
}
