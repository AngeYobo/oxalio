package com.oxalio.invoice.controller;

import com.oxalio.invoice.dto.*;
import com.oxalio.invoice.entity.TenantEntity;
import com.oxalio.invoice.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Gestion des tenants (clients OXALIO)")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TenantController {

    private final TenantService tenantService;

    // ============================================================
    // CREATE - Inscription nouveau tenant
    // ============================================================
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouveau tenant")
    public ResponseEntity<TenantDTO> registerTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        log.info("Nouvelle inscription tenant: {} ({})", request.getCompanyName(), request.getNcc());
        
        TenantEntity tenant = tenantService.createTenant(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(tenant));
    }

    // ============================================================
    // READ - Liste tous les tenants (ADMIN ONLY)
    // ============================================================
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Lister tous les tenants (Admin OXALIO uniquement)")
    public ResponseEntity<List<TenantDTO>> getAllTenants(
            @RequestParam(required = false) Boolean activeOnly
    ) {
        List<TenantEntity> tenants;
        
        if (Boolean.TRUE.equals(activeOnly)) {
            tenants = tenantService.getAllActiveTenants();
        } else {
            tenants = tenantService.getAllTenants();
        }
        
        List<TenantDTO> dtos = tenants.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // ============================================================
    // READ - Obtenir un tenant par ID
    // ============================================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un tenant par ID")
    public ResponseEntity<TenantDTO> getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id)
                .map(tenant -> ResponseEntity.ok(toDTO(tenant)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // READ - Obtenir un tenant par NCC
    // ============================================================
    @GetMapping("/ncc/{ncc}")
    @Operation(summary = "Obtenir un tenant par NCC")
    public ResponseEntity<TenantDTO> getTenantByNcc(@PathVariable String ncc) {
        return tenantService.getTenantByNcc(ncc)
                .map(tenant -> ResponseEntity.ok(toDTO(tenant)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // READ - Obtenir un tenant par slug
    // ============================================================
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtenir un tenant par slug")
    public ResponseEntity<TenantDTO> getTenantBySlug(@PathVariable String slug) {
        return tenantService.getTenantBySlug(slug)
                .map(tenant -> ResponseEntity.ok(toDTO(tenant)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // UPDATE - Mettre à jour un tenant
    // ============================================================
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un tenant")
    public ResponseEntity<TenantDTO> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        log.info("Mise à jour tenant: {}", id);
        
        TenantEntity updated = tenantService.updateTenant(id, request);
        
        return ResponseEntity.ok(toDTO(updated));
    }

    // ============================================================
    // UPDATE - Changer le plan d'abonnement
    // ============================================================
    @PutMapping("/{id}/subscription/upgrade")
    @Operation(summary = "Upgrader le plan d'abonnement")
    public ResponseEntity<TenantDTO> upgradePlan(
            @PathVariable Long id,
            @RequestParam String newPlan
    ) {
        log.info("Upgrade plan tenant {} vers {}", id, newPlan);
        
        TenantEntity upgraded = tenantService.upgradePlan(id, newPlan);
        
        return ResponseEntity.ok(toDTO(upgraded));
    }

    // ============================================================
    // UPDATE - Activer/Désactiver un tenant
    // ============================================================
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activer/Désactiver un tenant (Admin uniquement)")
    public ResponseEntity<TenantDTO> toggleTenantStatus(
            @PathVariable Long id,
            @RequestParam Boolean active
    ) {
        log.info("Changement statut tenant {} -> {}", id, active);
        
        TenantEntity tenant = tenantService.setTenantStatus(id, active);
        
        return ResponseEntity.ok(toDTO(tenant));
    }

    // ============================================================
    // DELETE - Supprimer un tenant
    // ============================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Supprimer un tenant (Admin uniquement)")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        log.warn("Suppression tenant: {}", id);
        
        tenantService.deleteTenant(id);
        
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // STATS - Statistiques des tenants (Admin)
    // ============================================================
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Statistiques des tenants")
    public ResponseEntity<TenantStatsDTO> getTenantStats() {
        TenantStatsDTO stats = tenantService.getTenantStats();
        return ResponseEntity.ok(stats);
    }

    // ============================================================
    // Mapper Entity -> DTO
    // ============================================================
    private TenantDTO toDTO(TenantEntity entity) {
        return TenantDTO.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .ncc(entity.getNcc())
                .slug(entity.getSlug())
                .fneEstablishment(entity.getFneEstablishment())
                .fnePointOfSale(entity.getFnePointOfSale())
                .subscriptionPlan(entity.getSubscriptionPlan())
                .subscriptionStatus(entity.getSubscriptionStatus())
                .subscriptionStartedAt(entity.getSubscriptionStartedAt())
                .subscriptionEndsAt(entity.getSubscriptionEndsAt())
                .monthlyInvoiceLimit(entity.getMonthlyInvoiceLimit())
                .monthlyInvoiceCount(entity.getMonthlyInvoiceCount())
                .ownerEmail(entity.getOwnerEmail())
                .ownerName(entity.getOwnerName())
                .ownerPhone(entity.getOwnerPhone())
                .logoUrl(entity.getLogoUrl())
                .isActive(entity.getIsActive())
                .isVerified(entity.getIsVerified())
                .createdAt(entity.getCreatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}