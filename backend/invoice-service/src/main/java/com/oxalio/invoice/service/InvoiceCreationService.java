// src/main/java/com/oxalio/invoice/service/InvoiceCreationService.java
package com.oxalio.invoice.service;

import com.oxalio.invoice.controller.dto.InvoiceCreateRequest;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import com.oxalio.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class InvoiceCreationService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public InvoiceEntity createInternalInvoice(InvoiceCreateRequest req) {

        // RNE: validation métier (en plus des annotations)
        boolean isRne = Boolean.TRUE.equals(req.getIsRne());
        if (isRne && (req.getRne() == null || req.getRne().isBlank())) {
            throw new IllegalArgumentException("rne est obligatoire quand isRne=true");
        }

        InvoiceEntity invoice = new InvoiceEntity();
        // Champs de base — adapte aux noms exacts de tes colonnes/champs InvoiceEntity
        invoice.setInvoiceType(req.getInvoiceType());
        invoice.setCurrency(req.getCurrency());
        invoice.setPaymentMethod(req.getPaymentMethod());
        invoice.setTemplate(req.getTemplate());
        invoice.setIsRne(isRne);
        invoice.setRne(req.getRne());

        invoice.setClientCompanyName(req.getClientCompanyName());
        invoice.setClientPhone(req.getClientPhone());
        invoice.setClientEmail(req.getClientEmail());
        invoice.setClientNcc(req.getClientNcc());

        invoice.setEstablishment(req.getEstablishment());
        invoice.setPointOfSale(req.getPointOfSale());
        invoice.setCommercialMessage(req.getCommercialMessage());

        // Statut initial
        invoice.setStatus(com.oxalio.invoice.model.InvoiceStatus.RECEIVED);

        // Lignes + calculs
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalVat = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (InvoiceCreateRequest.Line l : req.getLines()) {
            InvoiceLineEntity line = new InvoiceLineEntity();
            line.setInvoice(invoice);

            line.setSku(l.getSku());
            line.setUnit(l.getUnit());
            line.setDescription(l.getDescription());

            BigDecimal qty = nvl(l.getQuantity()).setScale(3, RoundingMode.HALF_UP);
            BigDecimal unitPrice = nvl(l.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal vatRate = nvl(l.getVatRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = nvl(l.getDiscount()).setScale(2, RoundingMode.HALF_UP);

            line.setQuantity(qty);
            line.setUnitPrice(unitPrice);
            line.setVatRate(vatRate);
            line.setDiscount(discount);

            // base = qty * unitPrice - discount
            BigDecimal base = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP)
                    .subtract(discount).max(BigDecimal.ZERO);

            BigDecimal vatAmount = base.multiply(vatRate)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal lineTotal = base.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

            line.setVatAmount(vatAmount);
            line.setLineTotal(lineTotal);

            invoice.getLines().add(line);

            subtotal = subtotal.add(base);
            totalVat = totalVat.add(vatAmount);
            totalDiscount = totalDiscount.add(discount);
        }

        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotalVat(totalVat.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotalAmount(subtotal.add(totalVat).setScale(2, RoundingMode.HALF_UP));
        invoice.setTotalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP));

        // Numéro interne si tu en as un (sinon laisse null et générer plus tard)
        // invoice.setInvoiceNumber(...)

        return invoiceRepository.save(invoice);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
