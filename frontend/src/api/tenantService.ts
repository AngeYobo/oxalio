/**
 * Tenant Service - API Calls
 * OXALIO FNE Platform
 */

import { apiClient } from './client';
import type {
  Tenant,
  CreateTenantRequest,
  UpdateTenantRequest,
  TenantStats,
} from '../types/tenant-types';

// ========================================
// Base URL
// ========================================

const BASE_URL = '/v1/tenants';

// ========================================
// CRUD Operations
// ========================================

/**
 * Inscription d'un nouveau tenant
 */
export const registerTenant = async (
  data: CreateTenantRequest
): Promise<Tenant> => {
  const response = await apiClient.post(`${BASE_URL}/register`, data);
  return response.data;
};

/**
 * Obtenir tous les tenants (Admin uniquement)
 */
export const getAllTenants = async (
  activeOnly?: boolean
): Promise<Tenant[]> => {
  const params = activeOnly ? { activeOnly: true } : {};
  const response = await apiClient.get(BASE_URL, { params });
  return response.data;
};

/**
 * Obtenir un tenant par ID
 */
export const getTenantById = async (id: number): Promise<Tenant> => {
  const response = await apiClient.get(`${BASE_URL}/${id}`);
  return response.data;
};

/**
 * Obtenir un tenant par NCC
 */
export const getTenantByNcc = async (ncc: string): Promise<Tenant> => {
  const response = await apiClient.get(`${BASE_URL}/ncc/${ncc}`);
  return response.data;
};

/**
 * Obtenir un tenant par slug
 */
export const getTenantBySlug = async (slug: string): Promise<Tenant> => {
  const response = await apiClient.get(`${BASE_URL}/slug/${slug}`);
  return response.data;
};

/**
 * Mettre à jour un tenant
 */
export const updateTenant = async (
  id: number,
  data: UpdateTenantRequest
): Promise<Tenant> => {
  const response = await apiClient.put(`${BASE_URL}/${id}`, data);
  return response.data;
};

/**
 * Upgrader le plan d'abonnement
 */
export const upgradeTenantPlan = async (
  id: number,
  newPlan: string
): Promise<Tenant> => {
  const response = await apiClient.put(
    `${BASE_URL}/${id}/subscription/upgrade`,
    null,
    { params: { newPlan } }
  );
  return response.data;
};

/**
 * Activer/Désactiver un tenant (Admin uniquement)
 */
export const toggleTenantStatus = async (
  id: number,
  active: boolean
): Promise<Tenant> => {
  const response = await apiClient.put(
    `${BASE_URL}/${id}/status`,
    null,
    { params: { active } }
  );
  return response.data;
};

/**
 * Supprimer un tenant (Admin uniquement)
 */
export const deleteTenant = async (id: number): Promise<void> => {
  await apiClient.delete(`${BASE_URL}/${id}`);
};

/**
 * Obtenir les statistiques des tenants (Admin uniquement)
 */
export const getTenantStats = async (): Promise<TenantStats> => {
  const response = await apiClient.get(`${BASE_URL}/stats`);
  return response.data;
};

// ========================================
// Helper Functions
// ========================================

/**
 * Vérifier si un NCC existe déjà
 */
export const checkNccExists = async (ncc: string): Promise<boolean> => {
  try {
    await getTenantByNcc(ncc);
    return true;
  } catch (error) {
    return false;
  }
};

/**
 * Calculer les jours restants d'essai
 */
export const calculateTrialDaysLeft = (tenant: Tenant): number => {
  const endDate = new Date(tenant.subscriptionEndsAt);
  const today = new Date();
  const diffTime = endDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return Math.max(0, diffDays);
};

/**
 * Calculer le pourcentage d'utilisation des factures
 */
export const calculateInvoiceUsagePercent = (tenant: Tenant): number => {
  if (tenant.monthlyInvoiceLimit === 0) return 0;
  return Math.round(
    (tenant.monthlyInvoiceCount / tenant.monthlyInvoiceLimit) * 100
  );
};