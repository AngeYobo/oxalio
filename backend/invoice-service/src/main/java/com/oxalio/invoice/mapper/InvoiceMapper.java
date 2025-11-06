package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.entity.InvoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "sellerTaxId", source = "seller.taxId")
    @Mapping(target = "sellerCompanyName", source = "seller.companyName")
    @Mapping(target = "sellerAddress", source = "seller.address")
    @Mapping(target = "buyerTaxId", source = "buyer.taxId")
    @Mapping(target = "buyerName", source = "buyer.name")
    @Mapping(target = "buyerAddress", source = "buyer.address")
    InvoiceEntity toEntity(InvoiceRequest request);

    InvoiceResponse toResponse(InvoiceEntity entity);

    List<InvoiceResponse> toResponseList(List<InvoiceEntity> entities);

    @Mapping(target = "sellerTaxId", source = "seller.taxId")
    @Mapping(target = "sellerCompanyName", source = "seller.companyName")
    @Mapping(target = "sellerAddress", source = "seller.address")
    @Mapping(target = "buyerTaxId", source = "buyer.taxId")
    @Mapping(target = "buyerName", source = "buyer.name")
    @Mapping(target = "buyerAddress", source = "buyer.address")
    void updateEntityFromRequest(InvoiceRequest request, @MappingTarget InvoiceEntity entity);
}
