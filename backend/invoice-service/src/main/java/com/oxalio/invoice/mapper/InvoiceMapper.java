package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceRequest.InvoiceLineDTO;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    // ===== Entity -> Response (header) =====
    @Mapping(target = "seller.companyName", source = "sellerCompanyName")
    @Mapping(target = "seller.taxId",       source = "sellerTaxId")
    @Mapping(target = "seller.address",     source = "sellerAddress")
    @Mapping(target = "buyer.name",         source = "buyerName")
    @Mapping(target = "buyer.taxId",        source = "buyerTaxId")
    @Mapping(target = "buyer.address",      source = "buyerAddress")
    @Mapping(target = "totals.subtotal",    source = "subtotal")
    @Mapping(target = "totals.totalVat",    source = "totalVat")
    @Mapping(target = "totals.totalAmount", source = "totalAmount")
    // ✅ Correction du nom : source entity.paymentMethod -> target response.paymentMode
    @Mapping(target = "paymentMode",        source = "paymentMethod") 
    @Mapping(target = "status",             expression = "java(entity.getStatus().name())")
    @Mapping(target = "lines",              source = "lines")
    @Mapping(target = "notes",   ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "seller.sellerDisplayName", source = "sellerDisplayName")
    @Mapping(target = "seller.pointOfSaleName",   source = "pointOfSaleName")
    @Mapping(target = "totals.otherTaxes",        source = "otherTaxes")
    @Mapping(target = "totals.totalToPay",        source = "totalToPay")
    // ✅ Ajout traçabilité RNE
    @Mapping(target = "template", source = "template")
    @Mapping(target = "isRne", source = "isRne")
    @Mapping(target = "rne", source = "rne")
    InvoiceResponse toResponse(InvoiceEntity entity);

    default List<InvoiceResponse> toResponseList(List<InvoiceEntity> entities) {
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ===== Entity -> Response (line) - Inchangé =====
    @Mapping(target = "description", source = "line.description")
    @Mapping(target = "quantity",    source = "line.quantity")
    @Mapping(target = "unitPrice",   source = "line.unitPrice")
    @Mapping(target = "vatRate",     source = "line.vatRate")
    @Mapping(target = "vatAmount",   source = "line.vatAmount")
    @Mapping(target = "discount",    source = "line.discount")
    @Mapping(target = "productCode", source = "line.productCode")
    @Mapping(target = "sku",         source = "line.sku")
    @Mapping(target = "unit",        source = "line.unit")
    @Mapping(target = "lineTotal",   source = "line.lineTotal")
    InvoiceResponse.InvoiceLineDTO toLineResponse(InvoiceLineEntity line);

    List<InvoiceResponse.InvoiceLineDTO> toLineResponseList(List<InvoiceLineEntity> lines);

    // ===== Request -> Entity (header) =====
    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "invoiceNumber",  ignore = true)
    @Mapping(target = "issueDate",      ignore = true)
    @Mapping(target = "stickerId",      ignore = true)
    @Mapping(target = "qrBase64",       ignore = true)
    @Mapping(target = "status",         ignore = true)
    @Mapping(target = "dgiReference",   ignore = true)
    @Mapping(target = "dgiSubmittedAt", ignore = true)
    @Mapping(target = "fneInvoiceId",   ignore = true)
    @Mapping(target = "fneReference",   ignore = true)
    @Mapping(target = "subtotal",       source = "totals.subtotal")
    @Mapping(target = "totalVat",       source = "totals.totalVat")
    @Mapping(target = "totalAmount",    source = "totals.totalAmount")
    // ✅ Correction du nom : source request.paymentMode -> target entity.paymentMethod
    @Mapping(target = "paymentMethod",  source = "paymentMode")
    @Mapping(target = "sellerCompanyName", source = "seller.companyName")
    @Mapping(target = "sellerTaxId",       source = "seller.taxId")
    @Mapping(target = "sellerAddress",     source = "seller.address")
    @Mapping(target = "buyerName",         source = "buyer.name")
    @Mapping(target = "buyerTaxId",        source = "buyer.taxId")
    @Mapping(target = "buyerAddress",      source = "buyer.address")
    @Mapping(target = "lines",             ignore = true)
    @Mapping(target = "sellerDisplayName", source = "seller.sellerDisplayName")
    @Mapping(target = "pointOfSaleName",   source = "seller.pointOfSaleName")
    @Mapping(target = "otherTaxes",        source = "totals.otherTaxes")
    @Mapping(target = "totalToPay",        source = "totals.totalToPay")
    // ✅ Ajout traçabilité RNE
    @Mapping(target = "template", source = "template")
    @Mapping(target = "isRne", source = "isRne")
    @Mapping(target = "rne", source = "rne")
    InvoiceEntity toEntity(InvoiceRequest request);

    // ===== Request -> Entity (line) - Inchangé =====
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "invoice",     ignore = true)
    @Mapping(target = "description", source = "line.description")
    @Mapping(target = "quantity",    source = "line.quantity")
    @Mapping(target = "unitPrice",   source = "line.unitPrice")
    @Mapping(target = "vatRate",     source = "line.vatRate")
    @Mapping(target = "vatAmount",   source = "line.vatAmount")
    @Mapping(target = "discount",    source = "line.discount")
    @Mapping(target = "productCode", source = "line.productCode")
    @Mapping(target = "sku",         source = "line.sku")
    @Mapping(target = "unit",        source = "line.unit")
    @Mapping(target = "lineTotal",   ignore = true)
    InvoiceLineEntity toLineEntity(InvoiceLineDTO line);

    List<InvoiceLineEntity> toLineEntityList(List<InvoiceLineDTO> lines);

    // ===== Patch (update) =====
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "currency",          source = "currency")
    @Mapping(target = "invoiceType",       source = "invoiceType")
    // ✅ Correction ici aussi
    @Mapping(target = "paymentMethod",     source = "paymentMode")
    @Mapping(target = "sellerCompanyName", source = "seller.companyName")
    @Mapping(target = "sellerTaxId",       source = "seller.taxId")
    @Mapping(target = "sellerAddress",     source = "seller.address")
    @Mapping(target = "buyerName",         source = "buyer.name")
    @Mapping(target = "buyerTaxId",        source = "buyer.taxId")
    @Mapping(target = "buyerAddress",      source = "buyer.address")
    @Mapping(target = "sellerDisplayName", source = "seller.sellerDisplayName")
    @Mapping(target = "pointOfSaleName",   source = "seller.pointOfSaleName")
    @Mapping(target = "template", source = "template")
    @Mapping(target = "isRne", source = "isRne")
    @Mapping(target = "rne", source = "rne")
    void updateEntityFromRequest(InvoiceRequest request, @MappingTarget InvoiceEntity entity);
}