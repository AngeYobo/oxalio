// Modèle léger pour le formulaire côté front (UI)
export type InvoiceType = 'STANDARD' | 'PROFORMA' | 'CREDIT_NOTE';
export type Currency = 'XOF' | 'USD' | 'EUR';
export type PaymentMode = 'CASH' | 'TRANSFER' | 'CARD' | 'MOBILE';

export interface InvoiceFormLine {
  description: string;
  quantity: number;
  unitPrice: number;
  vatRate: number;     // ex: 0.18
  discount?: number;   // montant (pas %), optionnel
  taxType?: string;
  productCode?: string; // optionnel
}

export interface InvoiceFormModel {
  invoiceType: InvoiceType;
  currency: Currency;
  buyerName: string;
  buyerTaxId: string;
  buyerAddress: string;
  lines: InvoiceFormLine[];
  paymentMode: PaymentMode;
  notes?: string;
}
