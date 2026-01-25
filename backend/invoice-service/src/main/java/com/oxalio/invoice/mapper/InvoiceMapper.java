package com.oxalio.invoice.mapper;

import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceRequest.InvoiceLineDTO;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct Mapper pour les conversions Invoice
 * 
 * Optimisations appliquées :
 * - Suppression des mappings redondants (MapStruct map automatiquement les champs identiques)
 * - Mapping automatique des nested objects (seller, buyer, totals)
 * - Utilisation de @InheritInverseConfiguration pour éviter la duplication
 * - Suppression des duplications de mapping (template mappé 2 fois -> erreur)
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface InvoiceMapper {

    // ════════════════════════════════════════════════════════════════
    // ENTITY -> RESPONSE
    // ════════════════════════════════════════════════════════════════

    /**
     * Convertit InvoiceEntity -> InvoiceResponse
     * 
     * MapStruct mappe automatiquement :
     * - Les champs avec le même nom (id, currency, invoiceType, etc.)
     * - Les nested objects si les noms correspondent (seller, buyer, totals)
     * - Les listes (lines)
     * 
     * On ne spécifie que les exceptions (noms différents ou logique custom)
     */
    @Mapping(target = "seller.companyName", source = "sellerCompanyName")
    @Mapping(target = "seller.taxId", source = "sellerTaxId")
    @Mapping(target = "seller.address", source = "sellerAddress")
    @Mapping(target = "seller.sellerDisplayName", source = "sellerDisplayName")
    @Mapping(target = "seller.pointOfSaleName", source = "pointOfSaleName")
    
    @Mapping(target = "buyer.name", source = "buyerName")
    @Mapping(target = "buyer.taxId", source = "buyerTaxId")
    @Mapping(target = "buyer.address", source = "buyerAddress")
    
    @Mapping(target = "totals.subtotal", source = "subtotal")
    @Mapping(target = "totals.totalVat", source = "totalVat")
    @Mapping(target = "totals.totalAmount", source = "totalAmount")
    @Mapping(target = "totals.otherTaxes", source = "otherTaxes")
    @Mapping(target = "totals.totalToPay", source = "totalToPay")
    
    // Champ avec nom différent
    @Mapping(target = "paymentMode", source = "paymentMethod")
    
    // Expression pour enum -> string
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    
    // Champs ignorés (non exposés dans l'API)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "message", ignore = true)
    
    InvoiceResponse toResponse(InvoiceEntity entity);

    /**
     * Conversion en liste
     * MapStruct génère automatiquement cette méthode si on la déclare
     */
    List<InvoiceResponse> toResponseList(List<InvoiceEntity> entities);

    // ════════════════════════════════════════════════════════════════
    // ENTITY LINE -> RESPONSE LINE
    // ════════════════════════════════════════════════════════════════

    /**
     * Convertit InvoiceLineEntity -> InvoiceResponse.InvoiceLineDTO
     * 
     * MapStruct mappe automatiquement les champs identiques :
     * - description, quantity, unitPrice, vatRate, vatAmount, discount
     * - productCode, sku, unit, lineTotal, fneItemId
     */
    InvoiceResponse.InvoiceLineDTO toLineResponse(InvoiceLineEntity line);

    /**
     * Conversion en liste (générée automatiquement par MapStruct)
     */
    List<InvoiceResponse.InvoiceLineDTO> toLineResponseList(List<InvoiceLineEntity> lines);

    // ════════════════════════════════════════════════════════════════
    // REQUEST -> ENTITY (CREATE)
    // ════════════════════════════════════════════════════════════════

    /**
     * Convertit InvoiceRequest -> InvoiceEntity
     * 
     * Inverse du mapping Entity -> Response
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "stickerId", ignore = true)
    @Mapping(target = "qrBase64", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dgiReference", ignore = true)
    @Mapping(target = "dgiSubmittedAt", ignore = true)
    @Mapping(target = "fneInvoiceId", ignore = true)
    @Mapping(target = "fneReference", ignore = true)
    @Mapping(target = "fneToken", ignore = true)
    @Mapping(target = "lines", ignore = true)
    
    @Mapping(target = "sellerCompanyName", source = "seller.companyName")
    @Mapping(target = "sellerTaxId", source = "seller.taxId")
    @Mapping(target = "sellerAddress", source = "seller.address")
    @Mapping(target = "sellerDisplayName", source = "seller.sellerDisplayName")
    @Mapping(target = "pointOfSaleName", source = "seller.pointOfSaleName")
    
    @Mapping(target = "buyerName", source = "buyer.name")
    @Mapping(target = "buyerTaxId", source = "buyer.taxId")
    @Mapping(target = "buyerAddress", source = "buyer.address")
    
    @Mapping(target = "subtotal", source = "totals.subtotal")
    @Mapping(target = "totalVat", source = "totals.totalVat")
    @Mapping(target = "totalAmount", source = "totals.totalAmount")
    @Mapping(target = "otherTaxes", source = "totals.otherTaxes")
    @Mapping(target = "totalToPay", source = "totals.totalToPay")
    
    // Champ avec nom différent
    @Mapping(target = "paymentMethod", source = "paymentMode")
    
    InvoiceEntity toEntity(InvoiceRequest request);

    // ════════════════════════════════════════════════════════════════
    // REQUEST LINE -> ENTITY LINE
    // ════════════════════════════════════════════════════════════════

    /**
     * Convertit InvoiceLineDTO -> InvoiceLineEntity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "fneItemId", ignore = true)
    // @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "lineTotal", ignore = true) // Calculé par la logique métier
    InvoiceLineEntity toLineEntity(InvoiceLineDTO line);

    /**
     * Conversion en liste
     */
    List<InvoiceLineEntity> toLineEntityList(List<InvoiceLineDTO> lines);

    // ════════════════════════════════════════════════════════════════
    // UPDATE (PATCH)
    // ════════════════════════════════════════════════════════════════

    /**
     * Met à jour une entité existante avec les données de la requête
     * 
     * @BeanMapping(ignoreByDefault = true) : seuls les champs explicitement mappés sont mis à jour
     * NullValuePropertyMappingStrategy.IGNORE : les valeurs null ne remplacent pas les valeurs existantes
     */
    @BeanMapping(
        ignoreByDefault = true,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "invoiceType", source = "invoiceType")
    @Mapping(target = "paymentMethod", source = "paymentMode")
    @Mapping(target = "template", source = "template")
    @Mapping(target = "isRne", source = "isRne")
    @Mapping(target = "rne", source = "rne")
    
    @Mapping(target = "sellerCompanyName", source = "seller.companyName")
    @Mapping(target = "sellerTaxId", source = "seller.taxId")
    @Mapping(target = "sellerAddress", source = "seller.address")
    @Mapping(target = "sellerDisplayName", source = "seller.sellerDisplayName")
    @Mapping(target = "pointOfSaleName", source = "seller.pointOfSaleName")
    
    @Mapping(target = "buyerName", source = "buyer.name")
    @Mapping(target = "buyerTaxId", source = "buyer.taxId")
    @Mapping(target = "buyerAddress", source = "buyer.address")
    
    void updateEntityFromRequest(InvoiceRequest request, @MappingTarget InvoiceEntity entity);
}