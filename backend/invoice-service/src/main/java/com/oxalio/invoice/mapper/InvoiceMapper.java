package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.entity.InvoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    // ⚡ plus besoin de @Mappings ici si les champs ont le même nom
    InvoiceEntity toEntity(InvoiceRequest request);

    InvoiceResponse toResponse(InvoiceEntity entity);

    List<InvoiceResponse> toResponseList(List<InvoiceEntity> entities);

    void updateEntityFromRequest(InvoiceRequest request, @MappingTarget InvoiceEntity entity);
}
