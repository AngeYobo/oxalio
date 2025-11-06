// src/types/invoice.ts

export type InvoiceType = 'STANDARD' | 'PROFORMA' | 'CREDIT_NOTE';
export type Currency = 'XOF' | 'USD' | 'EUR';
export type PaymentMode = 'CASH' | 'TRANSFER' | 'CARD' | 'MOBILE';
export type InvoiceStatus = 'RECEIVED' | 'SUBMITTED_TO_DGI' | 'REJECTED' | 'CANCELLED';

export interface SellerDTO {
  taxId: string;
  companyName: string;
  address: string;
  email?: string;
  phone?: string;
}

export interface BuyerDTO {
  taxId: string;
  name: string;
  address: string;
  email?: string;
  phone?: string;
}

export interface InvoiceLineDTO {
  description: string;
  quantity: number;
  unitPrice: number;
  vatRate: number;
  vatAmount: number;
  discount: number;
  productCode?: string;
}

export interface TotalsDTO {
  subtotal: number;
  totalVat: number;
  totalAmount: number;
  totalDiscount: number;
}

export interface InvoiceRequest {
  invoiceType: string;
  currency: string;
  seller: SellerDTO;
  buyer: BuyerDTO;
  lines: InvoiceLineDTO[];
  totals: TotalsDTO;
  paymentMode: string;
  notes?: string;
}

export interface InvoiceLineResponse extends InvoiceLineDTO {
  lineTotal: number;
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  invoiceType: string;
  currency: string;
  issueDate: string;
  seller: SellerDTO;
  buyer: BuyerDTO;
  lines: InvoiceLineResponse[];
  totals: TotalsDTO;
  paymentMode: string;
  stickerId: string;
  qrBase64: string;
  status: string;
  dgiReference?: string;
  dgiSubmittedAt?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}