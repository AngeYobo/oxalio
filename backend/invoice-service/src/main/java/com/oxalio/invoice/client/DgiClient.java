// src/main/java/com/oxalio/invoice/client/DgiClient.java
package com.oxalio.invoice.client;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.mapper.InvoiceDgiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DgiClient {

    private final InvoiceDgiMapper dgiMapper;

    public InvoiceResponse submit(InvoiceRequest request) {
        // Construire le payload attendu par la DGI
        var payload = dgiMapper.toDgiPayload(request);

        // TODO: appel HTTP réel ici
        log.info("[MOCK DGI] Soumission payload: type={}, currency={}, paymentMode={}",
                payload.getInvoiceType(), payload.getCurrency(), payload.getPaymentMode());

        // Simuler une réponse "OK"
        return InvoiceResponse.builder()
                .status("SUBMITTED_TO_DGI")
                .dgiReference("DGI-" + java.util.UUID.randomUUID())
                .stickerId("STKR-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("Soumission DGI simulée avec succès") // nécessite le champ dans DTO
                .build();
    }

    public InvoiceResponse simulateError(String reason) {
        return InvoiceResponse.builder()
                .status("ERROR")
                .message(reason) // nécessite le champ dans DTO
                .build();
    }
}
