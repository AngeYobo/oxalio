package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.model.InvoiceDTO;
import com.oxalio.invoice.model.SellerDTO;
import com.oxalio.invoice.model.BuyerDTO;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class InvoiceDgiMapper {

    public static InvoiceRequest fromInvoiceModel(InvoiceDTO invoice) {
        InvoiceRequest dto = new InvoiceRequest();

        dto.setInvoiceType(invoice.getInvoiceType());
        dto.setCurrency(invoice.getCurrency());
        dto.setPaymentMode(invoice.getPaymentMode());

        // ✅ Seller
        SellerDTO seller = SellerDTO.builder()
                .taxId(invoice.getSeller().getTaxId())
                .companyName(invoice.getSeller().getCompanyName())
                .address(invoice.getSeller().getAddress())
                .build();
        dto.setSeller(seller);

        // ✅ Buyer
        BuyerDTO buyer = BuyerDTO.builder()
                .taxId(invoice.getBuyer().getTaxId())
                .name(invoice.getBuyer().getName())
                .address(invoice.getBuyer().getAddress())
                .build();
        dto.setBuyer(buyer);

        // ✅ Lines
        dto.setLines(invoice.getLines().stream().map(l ->
                InvoiceRequest.InvoiceLineDTO.builder()
                        .description(l.getDescription())
                        .quantity(BigDecimal.valueOf(l.getQuantity()))
                        .unitPrice(BigDecimal.valueOf(l.getUnitPrice()))
                        .vatRate(BigDecimal.valueOf(l.getVatRate()))
                        .vatAmount(BigDecimal.valueOf(l.getVatAmount()))
                        .discount(BigDecimal.valueOf(l.getDiscount()))
                        .build()
        ).collect(Collectors.toList()));

        // ✅ Totals
        dto.setTotals(
                InvoiceRequest.TotalsDTO.builder()
                        .subtotal(BigDecimal.valueOf(invoice.getTotals().getSubtotal()))
                        .totalVat(BigDecimal.valueOf(invoice.getTotals().getTotalVat()))
                        .totalAmount(BigDecimal.valueOf(invoice.getTotals().getTotalAmount()))
                        .build()
        );

        return dto;
    }
}
