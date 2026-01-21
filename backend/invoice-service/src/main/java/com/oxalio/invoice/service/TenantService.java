package com.oxalio.invoice.service;

import com.oxalio.invoice.entity.TenantEntity;
import com.oxalio.invoice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oxalio.invoice.dto.CreateTenantRequest;
import com.oxalio.invoice.dto.UpdateTenantRequest;
import com.oxalio.invoice.dto.TenantStatsDTO;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {
    
    private final TenantRepository tenantRepository;
    
    @Transactional
    public TenantEntity createTenant(TenantEntity tenant) {
        // Générer le slug à partir du nom de l'entreprise
        if (tenant.getSlug() == null) {
            tenant.setSlug(generateSlug(tenant.getCompanyName()));
        }
        
        // Vérifier unicité du NCC et email
        if (tenantRepository.existsByNcc(tenant.getNcc())) {
            throw new IllegalArgumentException("Un tenant avec ce NCC existe déjà");
        }
        
        if (tenantRepository.existsByOwnerEmail(tenant.getOwnerEmail())) {
            throw new IllegalArgumentException("Un tenant avec cet email existe déjà");
        }
        
        // Période d'essai de 30 jours
        tenant.setSubscriptionStartedAt(LocalDateTime.now());
        tenant.setSubscriptionEndsAt(LocalDateTime.now().plusDays(30));
        tenant.setSubscriptionStatus("trial");
        
        log.info("Création nouveau tenant: {} ({})", tenant.getCompanyName(), tenant.getNcc());
        
        return tenantRepository.save(tenant);
    }
    
    public Optional<TenantEntity> getTenantById(Long id) {
        return tenantRepository.findById(id);
    }
    
    public Optional<TenantEntity> getTenantByNcc(String ncc) {
        return tenantRepository.findByNcc(ncc);
    }
    
    public Optional<TenantEntity> getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }
    
    public List<TenantEntity> getAllActiveTenants() {
        return tenantRepository.findByIsActive(true);
    }
    
    @Transactional
    public void incrementInvoiceCount(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant introuvable"));
        
        tenant.setMonthlyInvoiceCount(tenant.getMonthlyInvoiceCount() + 1);
        
        // Vérifier la limite
        if (tenant.getMonthlyInvoiceCount() > tenant.getMonthlyInvoiceLimit()) {
            log.warn("Tenant {} a dépassé sa limite de factures", tenantId);
            throw new IllegalStateException("Limite de factures atteinte. Veuillez upgrader votre abonnement.");
        }
        
        tenantRepository.save(tenant);
    }
    
    @Transactional
    public void resetMonthlyCounters() {
        log.info("Réinitialisation des compteurs mensuels pour tous les tenants");
        tenantRepository.resetAllMonthlyCounters();
    }
    
    private String generateSlug(String companyName) {
        return companyName
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    }

    @Transactional
public TenantEntity createTenant(CreateTenantRequest request) {
    // Générer le slug
    String slug = generateSlug(request.getCompanyName());
    
    // Vérifier unicité
    if (tenantRepository.existsByNcc(request.getNcc())) {
        throw new IllegalArgumentException("Un tenant avec ce NCC existe déjà");
    }
    
    if (tenantRepository.existsByOwnerEmail(request.getOwnerEmail())) {
        throw new IllegalArgumentException("Un tenant avec cet email existe déjà");
    }
    
    // Créer le tenant
    TenantEntity tenant = TenantEntity.builder()
            .companyName(request.getCompanyName())
            .ncc(request.getNcc())
            .slug(slug)
            .fneApiKey(request.getFneApiKey())
            .fneEstablishment(request.getFneEstablishment() != null ? request.getFneEstablishment() : "Siège")
            .fnePointOfSale(request.getFnePointOfSale() != null ? request.getFnePointOfSale() : "Caisse 1")
            .subscriptionPlan(request.getSubscriptionPlan() != null ? request.getSubscriptionPlan() : "starter")
            .subscriptionStatus("trial")
            .subscriptionStartedAt(LocalDateTime.now())
            .subscriptionEndsAt(LocalDateTime.now().plusDays(30))
            .monthlyInvoiceLimit(50)
            .monthlyInvoiceCount(0)
            .ownerEmail(request.getOwnerEmail())
            .ownerName(request.getOwnerName())
            .ownerPhone(request.getOwnerPhone())
            .isActive(true)
            .isVerified(false)
            .build();
    
    log.info("Création tenant: {} ({})", tenant.getCompanyName(), tenant.getNcc());
    
    return tenantRepository.save(tenant);
}

public List<TenantEntity> getAllTenants() {
    return tenantRepository.findAll();
}

@Transactional
public TenantEntity updateTenant(Long id, UpdateTenantRequest request) {
    TenantEntity tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant introuvable"));
    
    if (request.getCompanyName() != null) {
        tenant.setCompanyName(request.getCompanyName());
    }
    if (request.getOwnerEmail() != null) {
        tenant.setOwnerEmail(request.getOwnerEmail());
    }
    if (request.getOwnerName() != null) {
        tenant.setOwnerName(request.getOwnerName());
    }
    if (request.getOwnerPhone() != null) {
        tenant.setOwnerPhone(request.getOwnerPhone());
    }
    if (request.getFneApiKey() != null) {
        tenant.setFneApiKey(request.getFneApiKey());
    }
    if (request.getLogoUrl() != null) {
        tenant.setLogoUrl(request.getLogoUrl());
    }
    if (request.getIsActive() != null) {
        tenant.setIsActive(request.getIsActive());
    }
    
    return tenantRepository.save(tenant);
    }

    @Transactional
    public TenantEntity upgradePlan(Long id, String newPlan) {
        TenantEntity tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant introuvable"));
        
        // Mettre à jour les limites selon le plan
        switch (newPlan.toLowerCase()) {
            case "starter":
                tenant.setMonthlyInvoiceLimit(50);
                break;
            case "professional":
                tenant.setMonthlyInvoiceLimit(500);
                break;
            case "enterprise":
                tenant.setMonthlyInvoiceLimit(999999);
                break;
            default:
                throw new IllegalArgumentException("Plan invalide");
        }
        
        tenant.setSubscriptionPlan(newPlan);
        tenant.setSubscriptionStatus("active");
        
        log.info("Tenant {} upgraded to {}", id, newPlan);
        
        return tenantRepository.save(tenant);
    }

    @Transactional
    public TenantEntity setTenantStatus(Long id, Boolean active) {
        TenantEntity tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant introuvable"));
        
        tenant.setIsActive(active);
        
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deleteTenant(Long id) {
        tenantRepository.deleteById(id);
    }

    public TenantStatsDTO getTenantStats() {
        Long total = tenantRepository.count();
        Long active = tenantRepository.countActiveTenants();
        Long starter = tenantRepository.countTenantsByPlan("starter");
        Long professional = tenantRepository.countTenantsByPlan("professional");
        Long enterprise = tenantRepository.countTenantsByPlan("enterprise");
        
        return TenantStatsDTO.builder()
                .totalTenants(total)
                .activeTenants(active)
                .starterPlanCount(starter)
                .professionalPlanCount(professional)
                .enterprisePlanCount(enterprise)
                .build();
    }
}