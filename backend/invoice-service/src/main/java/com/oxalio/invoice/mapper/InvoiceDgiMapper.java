// src/main/java/com/oxalio/invoice/mapper/InvoiceDgiMapper.java
package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.InvoiceRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceDgiMapper {

    /**
     * Mapping STRICT vers le payload DGI selon les champs réellement présents
     * dans InvoiceRequest et compatibles avec InvoiceEntity.
     */
    public DgiPayload toDgiPayload(InvoiceRequest req) {
        DgiPayload p = new DgiPayload();

        // --- Header ---
        p.setInvoiceType(req.getInvoiceType());
        p.setCurrency(req.getCurrency());
        p.setPaymentMode(req.getPaymentMode());
        p.setNotes(req.getNotes());

        // --- Seller ---
        var sReq = req.getSeller();
        DgiPayload.Seller s = new DgiPayload.Seller();
        if (sReq != null) {
            s.setCompanyName(sReq.getCompanyName());
            s.setTaxId(sReq.getTaxId());
            s.setAddress(sReq.getAddress());
            s.setPhone(sReq.getPhone());
            s.setEmail(sReq.getEmail());

            // 🔥 Champs supplémentaires réellement existants
            s.setSellerDisplayName(sReq.getSellerDisplayName());
            s.setPointOfSaleName(sReq.getPointOfSaleName());
        }
        p.setSeller(s);

        // --- Buyer ---
        var bReq = req.getBuyer();
        DgiPayload.Buyer b = new DgiPayload.Buyer();
        if (bReq != null) {
            b.setName(bReq.getName());
            b.setTaxId(bReq.getTaxId());
            b.setAddress(bReq.getAddress());
            b.setPhone(bReq.getPhone());
            b.setEmail(bReq.getEmail());
        }
        p.setBuyer(b);

        // --- Lines ---
        List<DgiPayload.Line> lines =
                req.getLines() == null ? List.of() :
                        req.getLines().stream().map(l -> {
                            DgiPayload.Line dl = new DgiPayload.Line();
                            dl.setDescription(l.getDescription());
                            dl.setQuantity(l.getQuantity());
                            dl.setUnitPrice(l.getUnitPrice());
                            dl.setVatRate(l.getVatRate());
                            dl.setVatAmount(l.getVatAmount());
                            dl.setDiscount(l.getDiscount());
                            dl.setProductCode(l.getProductCode());

                            // 🔥 Maintenant présents dans InvoiceRequest.InvoiceLineDTO
                            dl.setSku(l.getSku());
                            dl.setUnit(l.getUnit());

                            return dl;
                        }).collect(Collectors.toList());

        p.setLines(lines);

        return p;
    }

    // ============================================================
    // INTERNAL PAYLOAD FOR FNE COMMUNICATION
    // ============================================================
    @lombok.Data
    public static class DgiPayload {
        private String invoiceType;
        private String currency;
        private String paymentMode;
        private String notes;

        private Seller seller;
        private Buyer buyer;
        private List<Line> lines;

        @lombok.Data
        public static class Seller {
            private String companyName;
            private String taxId;
            private String address;
            private String phone;
            private String email;

            // 🔥 Champs ajoutés dans SellerDTO
            private String sellerDisplayName;
            private String pointOfSaleName;
        }

        @lombok.Data
        public static class Buyer {
            private String name;
            private String taxId;
            private String address;
            private String phone;
            private String email;
        }

        @lombok.Data
        public static class Line {
            private String description;
            private java.math.BigDecimal quantity;
            private java.math.BigDecimal unitPrice;
            private java.math.BigDecimal vatRate;
            private java.math.BigDecimal vatAmount;
            private java.math.BigDecimal discount;
            private String productCode;

            private String sku;
            private String unit;
        }
    }
}
