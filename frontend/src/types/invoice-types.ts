/**
 * Invoice Types - Compatible avec composants existants
 * OXALIO FNE - Frontend
 */

// ========================================
// Enums
// ========================================

export enum InvoiceStatus {
  DRAFT = 'draft',
  CERTIFIED = 'certified',
  PAID = 'paid',
  CANCELLED = 'cancelled',
}

export enum InvoiceType {
  SALE = 'sale',
  PURCHASE = 'purchase',
}

export enum InvoiceTemplate {
  B2B = 'B2B',
  B2C = 'B2C',
  B2F = 'B2F',
  B2G = 'B2G',
}

export enum PaymentMethod {
  CASH = 'cash',
  CARD = 'card',
  CHECK = 'check',
  MOBILE_MONEY = 'mobile-money',
  TRANSFER = 'transfer',
  DEFERRED = 'deferred',
}

export enum TaxType {
  TVA = 'TVA',
  TVAB = 'TVAB',
  TVAC = 'TVAC',
  TVAD = 'TVAD',
}

export enum UserRole {
  ADMIN = 'admin',
  USER = 'user',
  ACCOUNTANT = 'accountant',
}

// ========================================
// Types pour CreateInvoice.tsx
// ========================================

export interface InvoiceRequest {
  clientName: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  invoiceType: string;
  template: string;
  paymentMethod: string;
  paymentMode: string;          // Alias pour paymentMethod
  isRne: boolean;
  rne?: string;
  currency: string;
  
  // Structure seller/buyer pour compatibilité
  seller: {
    taxId: string;
    companyName: string;
    address: string;
    email: string;
    phone: string;
  };
  
  buyer: {
    taxId: string;
    name: string;
    address: string;
    email: string;
    phone: string;
  };
  
  // Lignes de facture
  lines: InvoiceLineDTO[];
  
  // Totaux calculés
  totals: {
    subtotal: number;
    totalVat: number;
    totalDiscount: number;
    totalAmount: number;
  };
  
  notes?: string;
  terms?: string;
}

export interface InvoiceLineDTO {
  reference?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  taxType: string;
  vatRate: number;              // Taux de TVA en pourcentage
  vatAmount: number;            // Montant TVA calculé
  discount: number;             // Remise
  discountPercent?: number;
  measurementUnit?: string;
  productCode?: string;         // Code produit
}

// ========================================
// Types pour InvoiceList.tsx et InvoiceDetail.tsx
// ========================================

export interface InvoiceResponse {
  id: number;
  invoiceNumber?: string;
  orderId?: number;
  
  // Client
  clientName: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  clientAddress?: string;
  
  // Structure seller/buyer
  seller: {
    taxId: string;
    companyName: string;
    address: string;
    email: string;
    phone: string;
  };
  
  buyer: {
    taxId: string;
    name: string;
    address: string;
    email: string;
    phone: string;
  };
  
  // Type et template
  invoiceType: string;
  template: string;
  paymentMethod: string;
  paymentMode?: string;
  currency: string;
  
  // Montants
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  
  // Totaux (structure complète)
  totals: {
    subtotal: number;
    totalVat: number;
    totalDiscount: number;
    totalAmount: number;
  };
  
  // Statut
  status: string;
  
  // RNE
  isRne: boolean;
  rne?: string;
  
  // Champs optionnels
  notes?: string;
  terms?: string;
  
  // Dates
  issueDate: string;
  createdAt: string;
  updatedAt: string;
  
  // FNE / DGI - Informations de certification
  fneReference?: string;
  fneInvoiceId?: string;
  fneToken?: string;
  fneQrCode?: string;
  fneCertifiedAt?: string;
  fneBalanceSticker?: number;
  
  // Anciens noms (compatibilité)
  dgiReference?: string;
  qrBase64?: string;
  dgiSubmittedAt?: string;
  
  // Relations
  lines?: InvoiceLineDTO[];
  items?: InvoiceLineDTO[];
}

// ========================================
// Types de base (pour compatibilité)
// ========================================

export interface BaseEntity {
  id: number;
  createdAt: string;
  updatedAt: string;
}

export interface User extends BaseEntity {
  email: string;
  name: string;
  role: UserRole;
  companyName: string;
  companyNcc: string;
  phone?: string;
  avatar?: string;
  lastLoginAt?: string;
  isActive: boolean;
}

export interface Invoice extends BaseEntity {
  invoiceNumber?: string;
  orderId?: number;
  clientName: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  clientAddress?: string;
  invoiceType: InvoiceType;
  template: InvoiceTemplate;
  paymentMethod: PaymentMethod;
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  status: InvoiceStatus;
  isRne: boolean;
  rne?: string;
  notes?: string;
  terms?: string;
  fneReference?: string;
  fneInvoiceId?: string;
  fneToken?: string;
  fneQrCode?: string;
  fneCertifiedAt?: string;
  fneBalanceSticker?: number;
  items: InvoiceItem[];
  user?: User;
}

export interface InvoiceItem extends BaseEntity {
  invoiceId: number;
  reference?: string;
  description: string;
  measurementUnit?: string;
  quantity: number;
  unitPrice: number;
  discountPercent: number;
  discountAmount: number;
  subtotal: number;
  subtotalAfterDiscount: number;
  taxAmount: number;
  totalAmount: number;
  taxType: TaxType;
  taxRate: number;
}

// ========================================
// DTOs
// ========================================

export interface CreateInvoiceDTO {
  invoiceType: InvoiceType;
  template: InvoiceTemplate;
  paymentMethod: PaymentMethod;
  clientName: string;
  clientNcc?: string;
  clientPhone?: string;
  clientEmail?: string;
  clientAddress?: string;
  isRne: boolean;
  rne?: string;
  items: CreateInvoiceItemDTO[];
  notes?: string;
  terms?: string;
  discountPercent?: number;
}

export interface CreateInvoiceItemDTO {
  reference?: string;
  description: string;
  measurementUnit?: string;
  quantity: number;
  unitPrice: number;
  taxType: TaxType;
  discountPercent?: number;
}

export interface UpdateInvoiceDTO {
  clientName?: string;
  clientPhone?: string;
  clientEmail?: string;
  status?: InvoiceStatus;
  notes?: string;
  terms?: string;
}

// ========================================
// FNE Types
// ========================================

export interface FneInvoiceRequest {
  invoiceType: InvoiceType;
  template: InvoiceTemplate;
  paymentMethod: PaymentMethod;
  isRne: boolean;
  rne?: string | null;
  clientNcc?: string;
  clientCompanyName: string;
  clientPhone: string;
  clientEmail: string;
  clientSellerName?: string;
  commercialMessage?: string;
  footer?: string;
  foreignCurrency?: string;
  foreignCurrencyRate?: number;
  items: FneInvoiceItemRequest[];
  discount?: number;
}

export interface FneInvoiceItemRequest {
  reference?: string;
  description: string;
  quantity: number;
  amount: number;
  taxes: TaxType[];
  discount?: number;
  measurementUnit?: string;
}

export interface FneInvoiceResponse {
  ncc: string;
  reference: string;
  token: string;
  qrCode?: string;
  warning: boolean;
  balanceSticker: number;
  invoice: FneInvoiceDetails;
}

export interface FneInvoiceDetails {
  id: string;
  reference: string;
  type: 'invoice' | 'refund';
  date: string;
  amount: number;
  vatAmount: number;
  status: string;
  template: InvoiceTemplate;
}

// ========================================
// Stats et Analytics
// ========================================

export interface InvoiceStats {
  total: number;
  draft: number;
  certified: number;
  paid: number;
  cancelled: number;
  totalAmount: number;
  totalVat: number;
  averageAmount: number;
  thisMonth: number;
  lastMonth: number;
  thisYear: number;
  growthRate: number;
}

export interface InvoiceFilters {
  status?: InvoiceStatus;
  template?: InvoiceTemplate;
  startDate?: string;
  endDate?: string;
  clientName?: string;
  fneReference?: string;
  minAmount?: number;
  maxAmount?: number;
  search?: string;
}

export interface PaginationParams {
  page: number;
  limit: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
  };
}

// ========================================
// API Response Types
// ========================================

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface ApiError {
  success: false;
  message: string;
  errors?: Record<string, string[]>;
  code?: string;
  status?: number;
}

// ========================================
// Constants
// ========================================

export const INVOICE_STATUS_LABELS: Record<InvoiceStatus, string> = {
  [InvoiceStatus.DRAFT]: 'Brouillon',
  [InvoiceStatus.CERTIFIED]: 'Certifiée',
  [InvoiceStatus.PAID]: 'Payée',
  [InvoiceStatus.CANCELLED]: 'Annulée',
};

export const INVOICE_TEMPLATE_LABELS: Record<InvoiceTemplate, string> = {
  [InvoiceTemplate.B2B]: 'Entreprise (B2B)',
  [InvoiceTemplate.B2C]: 'Particulier (B2C)',
  [InvoiceTemplate.B2F]: 'Export (B2F)',
  [InvoiceTemplate.B2G]: 'État (B2G)',
};

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  [PaymentMethod.CASH]: 'Espèces',
  [PaymentMethod.CARD]: 'Carte bancaire',
  [PaymentMethod.CHECK]: 'Chèque',
  [PaymentMethod.MOBILE_MONEY]: 'Mobile Money',
  [PaymentMethod.TRANSFER]: 'Virement',
  [PaymentMethod.DEFERRED]: 'À terme',
};

export const TAX_TYPE_LABELS: Record<TaxType, string> = {
  [TaxType.TVA]: 'TVA 18%',
  [TaxType.TVAB]: 'TVA 9%',
  [TaxType.TVAC]: 'TVA 0% (Exo. Conv.)',
  [TaxType.TVAD]: 'TVA 0% (Exo. Lég.)',
};

export const TAX_RATES: Record<TaxType, number> = {
  [TaxType.TVA]: 0.18,
  [TaxType.TVAB]: 0.09,
  [TaxType.TVAC]: 0.00,
  [TaxType.TVAD]: 0.00,
};

// ========================================
// Type Guards
// ========================================

export const isInvoice = (obj: any): obj is Invoice => {
  return (
    obj &&
    typeof obj.id === 'number' &&
    typeof obj.clientName === 'string' &&
    typeof obj.totalAmount === 'number'
  );
};

export const isInvoiceItem = (obj: any): obj is InvoiceItem => {
  return (
    obj &&
    typeof obj.description === 'string' &&
    typeof obj.quantity === 'number' &&
    typeof obj.unitPrice === 'number'
  );
};