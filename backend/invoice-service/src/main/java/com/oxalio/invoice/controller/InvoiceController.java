package com.oxalio.invoice.controller;

import com.oxalio.invoice.dto.DgiInvoiceResponseDTO;
import com.oxalio.invoice.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/mock/send")
    public ResponseEntity<DgiInvoiceResponseDTO> sendMock() {
        return ResponseEntity.ok(invoiceService.sendMockInvoiceToDgi());
    }
}
