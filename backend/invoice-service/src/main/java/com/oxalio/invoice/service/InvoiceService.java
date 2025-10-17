package com.oxalio.invoice.service;

import com.oxalio.invoice.client.DgiClient;
import com.oxalio.invoice.dto.DgiInvoiceRequestDTO;
import com.oxalio.invoice.dto.DgiInvoiceResponseDTO;
import com.oxalio.invoice.mapper.InvoiceDgiMapper;
import com.oxalio.invoice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final DgiClient dgiClient;

    public InvoiceService(DgiClient dgiClient) {
        this.dgiClient = dgiClient;
    }

    /**
     * ✅ Étape 1 : Génère une facture mock prête à l'envoi.
     */
    public InvoiceDTO createMockInvoice() {
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setInvoiceNumber("INV-2025-0001");
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setCurrency("XOF");
        invoice.setInvoiceType("STANDARD");
        invoice.setPaymentMode("CASH");

        SellerDTO seller = new SellerDTO();
        seller.setTaxId("NIF123456A");
        seller.setCompanyName("Oxalio SARL");
        seller.setAddress("Abidjan, Côte d’Ivoire");

        BuyerDTO buyer = new BuyerDTO();
        buyer.setTaxId("NIF789012B");
        buyer.setName("Client Démo");
        buyer.setAddress("Bouaké, Côte d’Ivoire");

        InvoiceLineDTO line = new InvoiceLineDTO();
        line.setDescription("Prestation de services Oxalio");
        line.setQuantity(1);
        line.setUnitPrice(10000);
        line.setVatRate(18);
        line.setVatAmount(1800);
        line.setDiscount(0);

        TotalsDTO totals = new TotalsDTO();
        totals.setSubtotal(10000);
        totals.setTotalVat(1800);
        totals.setTotalAmount(11800);

        invoice.setSeller(seller);
        invoice.setBuyer(buyer);
        invoice.setLines(List.of(line));
        invoice.setTotals(totals);
        invoice.setStatus("MOCK_READY");
        invoice.setHash("mock-hash");
        invoice.setSignature("mock-signature");
        invoice.setQrCode("mock-qr");

        return invoice;
    }

    /**
     * ✅ Étape 2 : Envoie la facture mockée vers la DGI via DgiClient.
     */
    public DgiInvoiceResponseDTO sendMockInvoiceToDgi() {
        InvoiceDTO mockInvoice = createMockInvoice();

        // Mapper la facture métier vers le format DGI
        DgiInvoiceRequestDTO request = InvoiceDgiMapper.fromInvoiceModel(mockInvoice);

        // Appel du client DGI
        DgiInvoiceResponseDTO response = dgiClient.sendInvoice(request);

        // Logging + futur stockage BDD si besoin
        log.info("[INVOICE] Facture {} envoyée à la DGI — statut={} message={}",
                mockInvoice.getInvoiceNumber(),
                response.getStatus(),
                response.getMessage());

        return response;
    }
}
