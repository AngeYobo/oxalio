/**
 * Invoice Service
 * OXALIO FNE - Frontend
 * 
 * Service pour gérer les factures locales (CRUD)
 * Différent de fneService qui gère la certification FNE
 */

import { apiClient, safeApiCall } from './client';

// ========================================
// Types
// ========================================

export interface Invoice {
  id: number;
  orderId?: number;
  
  // Client
  clientName: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  
  // Montants
  totalAmount: number;
  vatAmount: number;
  
  // Statut
  status: 'draft' | 'certified' | 'paid' | 'cancelled';
  
  // Dates
  createdAt: string;
  updatedAt: string;
  
  // FNE
  fneReference?: string;
  fneInvoiceId?: string;
  fneToken?: string;
  fneQrCode?: string;
  fneCertifiedAt?: string;
  fneBalanceSticker?: number;
  
  // Articles
  items: InvoiceItem[];
}

export interface InvoiceItem {
  id?: number;
  invoiceId?: number;
  reference?: string;
  description: string;
  quantity: number;
  amount: number;
  discount?: number;
  measurementUnit?: string;
  vatRate: number;
  vatAmount: number;
  totalAmount: number;
}

export interface CreateInvoiceDTO {
  orderId?: number;
  clientName: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  items: CreateInvoiceItemDTO[];
}

export interface CreateInvoiceItemDTO {
  reference?: string;
  description: string;
  quantity: number;
  amount: number;
  discount?: number;
  measurementUnit?: string;
  vatRate: number;
}

export interface UpdateInvoiceDTO {
  clientName?: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  status?: Invoice['status'];
}

export interface InvoiceFilters {
  status?: Invoice['status'];
  startDate?: string;
  endDate?: string;
  clientName?: string;
  fneReference?: string;
  minAmount?: number;
  maxAmount?: number;
}

export interface InvoiceStats {
  total: number;
  draft: number;
  certified: number;
  paid: number;
  cancelled: number;
  totalAmount: number;
  totalVat: number;
  averageAmount: number;
}

// ========================================
// Service
// ========================================

export const invoiceService = {
  /**
   * Récupérer toutes les factures
   */
  async getInvoices(filters?: InvoiceFilters): Promise<Invoice[]> {
    const params = new URLSearchParams();
    
    if (filters) {
      if (filters.status) params.append('status', filters.status);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.clientName) params.append('clientName', filters.clientName);
      if (filters.fneReference) params.append('fneReference', filters.fneReference);
      if (filters.minAmount) params.append('minAmount', filters.minAmount.toString());
      if (filters.maxAmount) params.append('maxAmount', filters.maxAmount.toString());
    }
    
    const response = await apiClient.get<Invoice[]>(
      `/invoices${params.toString() ? '?' + params.toString() : ''}`
    );
    return response.data;
  },

  /**
   * Récupérer une facture par ID
   */
  async getInvoice(id: number): Promise<Invoice> {
    const response = await apiClient.get<Invoice>(`/invoices/${id}`);
    return response.data;
  },

  /**
   * Récupérer une facture par référence FNE
   */
  async getInvoiceByFneReference(reference: string): Promise<Invoice> {
    const response = await apiClient.get<Invoice>(`/invoices/fne/${reference}`);
    return response.data;
  },

  /**
   * Créer une nouvelle facture
   */
  async createInvoice(data: CreateInvoiceDTO): Promise<Invoice> {
    const response = await apiClient.post<Invoice>('/invoices', data);
    return response.data;
  },

  /**
   * Créer une facture avec gestion d'erreur simplifiée
   */
  async safeCreateInvoice(data: CreateInvoiceDTO) {
    return safeApiCall(() => 
      apiClient.post<Invoice>('/invoices', data)
    );
  },

  /**
   * Mettre à jour une facture
   */
  async updateInvoice(id: number, data: UpdateInvoiceDTO): Promise<Invoice> {
    const response = await apiClient.put<Invoice>(`/invoices/${id}`, data);
    return response.data;
  },

  /**
   * Supprimer une facture
   */
  async deleteInvoice(id: number): Promise<void> {
    await apiClient.delete(`/invoices/${id}`);
  },

  /**
   * Obtenir les statistiques des factures
   */
  async getStats(startDate?: string, endDate?: string): Promise<InvoiceStats> {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await apiClient.get<InvoiceStats>(
      `/invoices/stats${params.toString() ? '?' + params.toString() : ''}`
    );
    return response.data;
  },

  /**
   * Obtenir les factures récentes
   */
  async getRecentInvoices(limit: number = 10): Promise<Invoice[]> {
    const response = await apiClient.get<Invoice[]>(`/invoices/recent?limit=${limit}`);
    return response.data;
  },

  /**
   * Exporter les factures en CSV
   */
  async exportToCSV(filters?: InvoiceFilters): Promise<Blob> {
    const params = new URLSearchParams();
    
    if (filters) {
      if (filters.status) params.append('status', filters.status);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
    }
    
    const response = await apiClient.get(
      `/invoices/export/csv${params.toString() ? '?' + params.toString() : ''}`,
      { responseType: 'blob' }
    );
    
    return response.data;
  },

  /**
   * Exporter les factures en Excel
   */
  async exportToExcel(filters?: InvoiceFilters): Promise<Blob> {
    const params = new URLSearchParams();
    
    if (filters) {
      if (filters.status) params.append('status', filters.status);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
    }
    
    const response = await apiClient.get(
      `/invoices/export/excel${params.toString() ? '?' + params.toString() : ''}`,
      { responseType: 'blob' }
    );
    
    return response.data;
  },
};

// ========================================
// Helpers
// ========================================

/**
 * Calculer le total HT d'une facture
 */
export const calculateInvoiceHT = (invoice: Invoice): number => {
  return invoice.totalAmount - invoice.vatAmount;
};

/**
 * Calculer le total d'un article
 */
export const calculateItemTotal = (item: CreateInvoiceItemDTO): number => {
  const subtotal = item.amount * item.quantity;
  const discount = item.discount || 0;
  const afterDiscount = subtotal * (1 - discount / 100);
  const vat = afterDiscount * item.vatRate;
  return afterDiscount + vat;
};

/**
 * Calculer le total d'une facture à partir des articles
 */
export const calculateTotalFromItems = (items: CreateInvoiceItemDTO[]): {
  totalHT: number;
  totalVAT: number;
  totalTTC: number;
} => {
  let totalHT = 0;
  let totalVAT = 0;
  
  items.forEach(item => {
    const subtotal = item.amount * item.quantity;
    const discount = item.discount || 0;
    const afterDiscount = subtotal * (1 - discount / 100);
    const vat = afterDiscount * item.vatRate;
    
    totalHT += afterDiscount;
    totalVAT += vat;
  });
  
  return {
    totalHT,
    totalVAT,
    totalTTC: totalHT + totalVAT,
  };
};

/**
 * Formater le statut de la facture
 */
export const formatInvoiceStatus = (status: Invoice['status']): string => {
  const statusMap: Record<Invoice['status'], string> = {
    draft: 'Brouillon',
    certified: 'Certifiée',
    paid: 'Payée',
    cancelled: 'Annulée',
  };
  
  return statusMap[status] || status;
};

/**
 * Obtenir la couleur du badge pour un statut
 */
export const getStatusColor = (status: Invoice['status']): string => {
  const colorMap: Record<Invoice['status'], string> = {
    draft: 'gray',
    certified: 'blue',
    paid: 'green',
    cancelled: 'red',
  };
  
  return colorMap[status] || 'gray';
};

/**
 * Vérifier si une facture est éditable
 */
export const isInvoiceEditable = (invoice: Invoice): boolean => {
  return invoice.status === 'draft' && !invoice.fneReference;
};

/**
 * Vérifier si une facture peut être certifiée
 */
export const canCertifyInvoice = (invoice: Invoice): boolean => {
  return invoice.status === 'draft' && !invoice.fneReference;
};

/**
 * Vérifier si une facture peut être annulée
 */
export const canCancelInvoice = (invoice: Invoice): boolean => {
  return invoice.status !== 'cancelled' && !invoice.fneReference;
};

export default invoiceService;