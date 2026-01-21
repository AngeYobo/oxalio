package com.oxalio.invoice.repository;

import com.oxalio.invoice.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, Long> {
    
    // Recherche par NCC
    Optional<TenantEntity> findByNcc(String ncc);
    
    // Recherche par slug
    Optional<TenantEntity> findBySlug(String slug);
    
    // Recherche par email du propriétaire
    Optional<TenantEntity> findByOwnerEmail(String ownerEmail);
    
    // Vérifier si existe par NCC
    boolean existsByNcc(String ncc);
    
    // Vérifier si existe par email
    boolean existsByOwnerEmail(String ownerEmail);
    
    // Vérifier si existe par slug
    boolean existsBySlug(String slug);
    
    // Tous les tenants actifs
    List<TenantEntity> findByIsActive(Boolean isActive);
    
    // Tenants par plan d'abonnement
    List<TenantEntity> findBySubscriptionPlan(String subscriptionPlan);
    
    // Tenants par statut d'abonnement
    List<TenantEntity> findBySubscriptionStatus(String subscriptionStatus);
    
    // Tenants avec abonnement expiré
    @Query("SELECT t FROM TenantEntity t WHERE t.subscriptionEndsAt < CURRENT_TIMESTAMP AND t.subscriptionStatus = 'active'")
    List<TenantEntity> findExpiredSubscriptions();
    
    // Réinitialiser les compteurs mensuels
    @Modifying
    @Query("UPDATE TenantEntity t SET t.monthlyInvoiceCount = 0")
    void resetAllMonthlyCounters();
    
    // Compter les tenants actifs
    @Query("SELECT COUNT(t) FROM TenantEntity t WHERE t.isActive = true")
    Long countActiveTenants();
    
    // Compter les tenants par plan
    @Query("SELECT COUNT(t) FROM TenantEntity t WHERE t.subscriptionPlan = :plan AND t.isActive = true")
    Long countTenantsByPlan(String plan);
}