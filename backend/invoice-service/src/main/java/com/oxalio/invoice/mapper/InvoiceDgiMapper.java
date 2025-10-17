package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.DgiInvoiceRequestDTO;
import com.oxalio.invoice.model.InvoiceDTO;
import com.oxalio.invoice.model.InvoiceLineDTO;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class InvoiceDgiMapper {

    /**
     * Convertit ta facture interne (InvoiceDTO) vers le format DGI.
     */
    public static DgiInvoiceRequestDTO fromInvoiceModel(InvoiceDTO invoice) {
        DgiInvoiceRequestDTO dto = new DgiInvoiceRequestDTO();
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceType(invoice.getInvoiceType());
        dto.setCurrency(invoice.getCurrency());
        dto.setIssueDate(invoice.getIssueDate().atZone(java.time.ZoneId.systemDefault()).toInstant());
        dto.setPaymentMode(invoice.getPaymentMode());

        // Seller
        DgiInvoiceRequestDTO.Seller s = new DgiInvoiceRequestDTO.Seller();
        s.taxId = invoice.getSeller().getTaxId();
        s.companyName = invoice.getSeller().getCompanyName();
        s.address = invoice.getSeller().getAddress();
        dto.setSeller(s);

        // Buyer
        DgiInvoiceRequestDTO.Buyer b = new DgiInvoiceRequestDTO.Buyer();
        b.taxId = invoice.getBuyer().getTaxId();
        b.name = invoice.getBuyer().getName();
        b.address = invoice.getBuyer().getAddress();
        dto.setBuyer(b);

        // Lines
        dto.setLines(invoice.getLines().stream().map(l -> {
            DgiInvoiceRequestDTO.LineItem line = new DgiInvoiceRequestDTO.LineItem();
            line.description = l.getDescription();
            line.quantity = BigDecimal.valueOf(l.getQuantity());
            line.unitPrice = BigDecimal.valueOf(l.getUnitPrice());
            line.vatRate = BigDecimal.valueOf(l.getVatRate());
            line.vatAmount = BigDecimal.valueOf(l.getVatAmount());
            line.discount = BigDecimal.valueOf(l.getDiscount());
            return line;
        }).collect(Collectors.toList()));

        // Totals
        DgiInvoiceRequestDTO.Totals totals = new DgiInvoiceRequestDTO.Totals();
        totals.subtotal = BigDecimal.valueOf(invoice.getTotals().getSubtotal());
        totals.totalVat = BigDecimal.valueOf(invoice.getTotals().getTotalVat());
        totals.totalAmount = BigDecimal.valueOf(invoice.getTotals().getTotalAmount());
        dto.setTotals(totals);

        return dto;
    }
}
