package com.oxalio.invoice.mock;

import com.oxalio.invoice.dto.InvoiceResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/mock-dgi")
public class MockDgiController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public InvoiceResponse receiveInvoice(@RequestBody String ignored) {
        InvoiceResponse resp = new InvoiceResponse();
        resp.setReference("MOCK-REF-123");
        resp.setStatus("ACCEPTED");
        resp.setMessage("Facture acceptée en mode mock local");
        resp.setSignature("sig");
        resp.setQrCode("qrcode");
        resp.setProcessedAt(Instant.now());
        return resp;
    }
}
