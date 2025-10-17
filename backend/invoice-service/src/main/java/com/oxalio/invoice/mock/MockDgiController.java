package com.oxalio.invoice.mock;

import com.oxalio.invoice.dto.DgiInvoiceResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/mock-dgi")
public class MockDgiController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DgiInvoiceResponseDTO receiveInvoice(@RequestBody String ignored) {
        DgiInvoiceResponseDTO resp = new DgiInvoiceResponseDTO();
        resp.setReference("MOCK-REF-123");
        resp.setStatus("ACCEPTED");
        resp.setMessage("Facture acceptée en mode mock local");
        resp.setSignature("sig");
        resp.setQrCode("qrcode");
        resp.setProcessedAt(Instant.now());
        return resp;
    }
}
