/**
 * FNE Service
 * OXALIO FNE - Frontend
 * 
 * Service pour interagir avec l'API FNE (Facture Normalisée Électronique)
 * Gère la création, la certification et la gestion des factures
 */

import { apiClient, safeApiCall } from './client';

// ========================================
// Types de requêtes FNE
// ========================================

export type InvoiceType = 'sale' | 'purchase';
export type InvoiceTemplate = 'B2B' | 'B2C' | 'B2F' | 'B2G';
export type PaymentMethod = 'cash' | 'card' | 'check' | 'mobile-money' | 'transfer' | 'deferred';
export type TaxType = 'TVA' | 'TVAB' | 'TVAC' | 'TVAD';

export interface CreateInvoiceRequest {
  // Type de facture
  invoiceType: InvoiceType;
  template: InvoiceTemplate;
  paymentMethod: PaymentMethod;
  
  // RNE (Reçu normalisé électronique)
  isRne: boolean;
  rne?: string | null;
  
  // Client
  clientNcc?: string;           // Obligatoire si B2B
  clientCompanyName: string;
  clientPhone: string;
  clientEmail: string;
  
  // Optionnel
  clientSellerName?: string;
  commercialMessage?: string;
  footer?: string;
  
  // Devise étrangère (pour B2F)
  foreignCurrency?: string;
  foreignCurrencyRate?: number;
  
  // Articles
  items: FneInvoiceItem[];
  
  // Remise globale
  discount?: number;
}

export interface FneInvoiceItem {
  reference?: string;
  description: string;
  quantity: number;
  amount: number;                  // Prix unitaire HT
  taxes: TaxType[];
  customTaxes?: CustomTax[];
  discount?: number;
  measurementUnit?: string;
}

export interface CustomTax {
  name: string;
  amount: number;                  // Taux en pourcentage
}

// ========================================
// Types de réponses FNE
// ========================================

export interface FneSignResponse {
  ncc: string;
  reference: string;               // Numéro de facture FNE
  token: string;                   // URL de vérification
  qrCode?: string;                 // QR Code en base64
  warning: boolean;
  balanceSticker: number;          // Quota de stickers restant
  invoice: FneInvoice;
}

export interface FneInvoice {
  id: string;                      // UUID de la facture FNE
  parentId: string | null;
  parentReference: string | null;
  token: string;
  reference: string;
  type: 'invoice' | 'refund';
  subtype: 'normal' | 'refund';
  date: string;
  paymentMethod: PaymentMethod;
  amount: number;                  // Montant total TTC
  vatAmount: number;               // Montant TVA
  fiscalStamp: number;
  discount: number;
  
  // Client
  clientNcc: string;
  clientCompanyName: string;
  clientPhone: string;
  clientEmail: string;
  clientTerminal: string | null;
  clientMerchantName: string | null;
  clientRccm: string | null;
  clientSellerName: string | null;
  clientEstablishment: string;
  clientPointOfSale: string;
  
  // Statut
  status: string;
  template: InvoiceTemplate;
  
  // Optionnel
  description: string | null;
  footer: string | null;
  commercialMessage: string | null;
  foreignCurrency: string | null;
  foreignCurrencyRate: number | null;
  
  // RNE
  isRne: boolean;
  rne: string | null;
  
  source: 'api' | 'web';
  createdAt: string;
  updatedAt: string;
  
  // Articles
  items: FneInvoiceItemResponse[];
  customTaxes: FneCustomTaxResponse[];
}

export interface FneInvoiceItemResponse {
  id: string;
  quantity: number;
  reference: string;
  description: string;
  amount: number;
  discount: number;
  measurementUnit: string | null;
  createdAt: string;
  updatedAt: string;
  taxes: FneTaxResponse[];
  customTaxes: FneCustomTaxResponse[];
  invoiceId: string;
  parentId: string | null;
}

export interface FneTaxResponse {
  invoiceItemId: string;
  vatRateId: string;
  amount: number;
  name: string;
  shortName: string;
  createdAt: string;
  updatedAt: string;
}

export interface FneCustomTaxResponse {
  id: string;
  invoiceId?: string;
  invoiceItemId?: string;
  amount: number;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRefundRequest {
  items: RefundItem[];
}

export interface RefundItem {
  id: string;                      // ID de l'article original
  quantity: number;                // Quantité à rembourser
}

export interface FneConfig {
  apiUrl: string;
  apiKey: string;
  establishment: {
    name: string;
    pointOfSale: string;
  };
  company: {
    ncc: string;
    name: string;
  };
}

// ========================================
// Service FNE
// ========================================

export const fneService = {
  /**
   * Créer et certifier une facture FNE
   */
  async createInvoice(data: CreateInvoiceRequest): Promise<FneSignResponse> {
    const response = await apiClient.post<FneSignResponse>(
      '/fne/invoices/sign',
      data
    );
    return response.data;
  },

  /**
   * Créer et certifier une facture avec gestion d'erreur simplifiée
   */
  async safeCreateInvoice(data: CreateInvoiceRequest) {
    return safeApiCall(() => 
      apiClient.post<FneSignResponse>('/fne/invoices/sign', data)
    );
  },

  /**
   * Créer un avoir (remboursement)
   */
  async createRefund(
    invoiceId: string, 
    data: CreateRefundRequest
  ): Promise<FneSignResponse> {
    const response = await apiClient.post<FneSignResponse>(
      `/fne/invoices/${invoiceId}/refund`,
      data
    );
    return response.data;
  },

  /**
   * Créer un avoir avec gestion d'erreur simplifiée
   */
  async safeCreateRefund(invoiceId: string, data: CreateRefundRequest) {
    return safeApiCall(() => 
      apiClient.post<FneSignResponse>(
        `/fne/invoices/${invoiceId}/refund`,
        data
      )
    );
  },

  /**
   * Récupérer la configuration FNE
   */
  async getConfig(): Promise<FneConfig> {
    const response = await apiClient.get<FneConfig>('/fne/config');
    return response.data;
  },

  /**
   * Vérifier la connexion à l'API FNE
   */
  async healthCheck(): Promise<boolean> {
    try {
      await apiClient.get('/fne/health');
      return true;
    } catch (error) {
      return false;
    }
  },
};

// ========================================
// Helpers / Utilitaires
// ========================================

/**
 * Calculer le montant HT d'un article
 */
export const calculateItemHT = (item: FneInvoiceItem): number => {
  const totalBeforeDiscount = item.amount * item.quantity;
  const discount = item.discount || 0;
  return totalBeforeDiscount * (1 - discount / 100);
};

/**
 * Calculer le montant TVA d'un article
 */
export const calculateItemVAT = (item: FneInvoiceItem): number => {
  const ht = calculateItemHT(item);
  const vatRate = item.taxes.includes('TVA') ? 0.18 : 
                  item.taxes.includes('TVAB') ? 0.09 : 0;
  return ht * vatRate;
};

/**
 * Calculer le montant total TTC d'un article
 */
export const calculateItemTTC = (item: FneInvoiceItem): number => {
  const ht = calculateItemHT(item);
  const vat = calculateItemVAT(item);
  return ht + vat;
};

/**
 * Calculer le montant total HT d'une facture
 */
export const calculateInvoiceHT = (items: FneInvoiceItem[]): number => {
  return items.reduce((sum, item) => sum + calculateItemHT(item), 0);
};

/**
 * Calculer le montant total TVA d'une facture
 */
export const calculateInvoiceVAT = (items: FneInvoiceItem[]): number => {
  return items.reduce((sum, item) => sum + calculateItemVAT(item), 0);
};

/**
 * Calculer le montant total TTC d'une facture
 */
export const calculateInvoiceTTC = (items: FneInvoiceItem[]): number => {
  return items.reduce((sum, item) => sum + calculateItemTTC(item), 0);
};

/**
 * Valider une requête de création de facture
 */
export const validateInvoiceRequest = (
  data: CreateInvoiceRequest
): { valid: boolean; errors: string[] } => {
  const errors: string[] = [];
  
  // Validation du client
  if (!data.clientCompanyName) {
    errors.push('Le nom du client est obligatoire');
  }
  if (!data.clientPhone) {
    errors.push('Le téléphone du client est obligatoire');
  }
  if (!data.clientEmail) {
    errors.push("L'email du client est obligatoire");
  }
  
  // Validation B2B
  if (data.template === 'B2B' && !data.clientNcc) {
    errors.push('Le NCC est obligatoire pour les factures B2B');
  }
  
  // Validation B2F
  if (data.template === 'B2F') {
    if (!data.foreignCurrency) {
      errors.push('La devise est obligatoire pour les factures B2F');
    }
    if (!data.foreignCurrencyRate || data.foreignCurrencyRate <= 0) {
      errors.push('Le taux de change est obligatoire pour les factures B2F');
    }
  }
  
  // Validation RNE
  if (data.isRne && !data.rne) {
    errors.push('Le numéro RNE est obligatoire si isRne est true');
  }
  
  // Validation des articles
  if (!data.items || data.items.length === 0) {
    errors.push('Au moins un article est requis');
  }
  
  data.items.forEach((item, index) => {
    if (!item.description) {
      errors.push(`Article ${index + 1}: la description est obligatoire`);
    }
    if (item.quantity <= 0) {
      errors.push(`Article ${index + 1}: la quantité doit être supérieure à 0`);
    }
    if (item.amount <= 0) {
      errors.push(`Article ${index + 1}: le montant doit être supérieur à 0`);
    }
    if (!item.taxes || item.taxes.length === 0) {
      errors.push(`Article ${index + 1}: au moins une taxe est requise`);
    }
  });
  
  return {
    valid: errors.length === 0,
    errors,
  };
};

/**
 * Générer un payload de test pour une facture B2C
 */
export const generateTestInvoiceB2C = (): CreateInvoiceRequest => {
  return {
    invoiceType: 'sale',
    template: 'B2C',
    paymentMethod: 'cash',
    isRne: false,
    rne: null,
    clientCompanyName: 'Client Test',
    clientPhone: '0700000000',
    clientEmail: 'test@example.com',
    clientSellerName: 'Ange KACOU',
    commercialMessage: 'Merci de votre visite !',
    items: [
      {
        description: 'Article de test',
        quantity: 1,
        amount: 10000,
        taxes: ['TVA'],
        measurementUnit: 'pièce',
      },
    ],
  };
};

export default fneService;