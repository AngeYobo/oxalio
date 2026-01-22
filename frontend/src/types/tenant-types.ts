/**
 * Tenant Types - Multi-tenant SaaS
 * OXALIO FNE Platform
 */

// ========================================
// Enums
// ========================================

export enum SubscriptionPlan {
  STARTER = 'starter',
  PROFESSIONAL = 'professional',
  ENTERPRISE = 'enterprise',
}

export enum SubscriptionStatus {
  TRIAL = 'trial',
  ACTIVE = 'active',
  SUSPENDED = 'suspended',
  CANCELLED = 'cancelled',
}

// ========================================
// Interfaces
// ========================================

export interface Tenant {
  id: number;
  
  // Identification
  companyName: string;
  ncc: string;
  slug: string;
  
  // FNE Configuration
  fneApiKey?: string;          // ✅ AJOUTÉ
  fneEstablishment: string;
  fnePointOfSale: string;
  
  // Subscription
  subscriptionPlan: SubscriptionPlan;
  subscriptionStatus: SubscriptionStatus;
  subscriptionStartedAt: string;
  subscriptionEndsAt: string;
  
  // Limites
  monthlyInvoiceLimit: number;
  monthlyInvoiceCount: number;
  
  // Contact
  ownerEmail: string;
  ownerName: string;
  ownerPhone?: string;
  
  // Logo
  logoUrl?: string;
  
  // Statut
  isActive: boolean;
  isVerified: boolean;
  
  // Metadata
  createdAt: string;
  lastLoginAt?: string;
}

export interface CreateTenantRequest {
  companyName: string;
  ncc: string;
  ownerEmail: string;
  ownerName: string;
  ownerPhone?: string;
  password: string;
  
  // FNE Configuration (optionnel)
  fneApiKey?: string;          
  fneEstablishment?: string;
  fnePointOfSale?: string;
  
  // Plan d'abonnement
  subscriptionPlan?: string;
}

export interface UpdateTenantRequest {
  companyName?: string;
  ownerEmail?: string;
  ownerName?: string;
  ownerPhone?: string;
  fneApiKey?: string;          
  fneEstablishment?: string;
  fnePointOfSale?: string;
  logoUrl?: string;
  isActive?: boolean;
}

export interface TenantStats {
  totalTenants: number;
  activeTenants: number;
  trialTenants: number;
  
  starterPlanCount: number;
  professionalPlanCount: number;
  enterprisePlanCount: number;
  
  totalInvoicesThisMonth: number;
  totalRevenue: number;
}

// ========================================
// Constants
// ========================================

export const SUBSCRIPTION_PLAN_LABELS: Record<SubscriptionPlan, string> = {
  [SubscriptionPlan.STARTER]: 'Starter',
  [SubscriptionPlan.PROFESSIONAL]: 'Professional',
  [SubscriptionPlan.ENTERPRISE]: 'Enterprise',
};

export const SUBSCRIPTION_PLAN_PRICES: Record<SubscriptionPlan, number> = {
  [SubscriptionPlan.STARTER]: 25000,
  [SubscriptionPlan.PROFESSIONAL]: 50000,
  [SubscriptionPlan.ENTERPRISE]: 150000,
};

export const SUBSCRIPTION_PLAN_LIMITS: Record<SubscriptionPlan, number> = {
  [SubscriptionPlan.STARTER]: 50,
  [SubscriptionPlan.PROFESSIONAL]: 500,
  [SubscriptionPlan.ENTERPRISE]: 999999,
};

export const SUBSCRIPTION_STATUS_LABELS: Record<SubscriptionStatus, string> = {
  [SubscriptionStatus.TRIAL]: 'Période d\'essai',
  [SubscriptionStatus.ACTIVE]: 'Actif',
  [SubscriptionStatus.SUSPENDED]: 'Suspendu',
  [SubscriptionStatus.CANCELLED]: 'Annulé',
};

export const SUBSCRIPTION_STATUS_COLORS: Record<SubscriptionStatus, string> = {
  [SubscriptionStatus.TRIAL]: 'bg-blue-100 text-blue-800',
  [SubscriptionStatus.ACTIVE]: 'bg-green-100 text-green-800',
  [SubscriptionStatus.SUSPENDED]: 'bg-orange-100 text-orange-800',
  [SubscriptionStatus.CANCELLED]: 'bg-red-100 text-red-800',
};