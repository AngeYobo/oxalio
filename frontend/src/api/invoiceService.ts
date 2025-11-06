// src/services/invoiceService.ts

import { InvoiceRequest, InvoiceResponse } from '../types/invoice';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

class InvoiceService {
  private async request<T>(
    endpoint: string,
    options?: RequestInit
  ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  /**
   * Récupère toutes les factures
   */
  async getAllInvoices(): Promise<InvoiceResponse[]> {
    return this.request<InvoiceResponse[]>('/invoices');
  }

  /**
   * Récupère une facture par son ID
   */
  async getInvoiceById(id: number): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/${id}`);
  }

  /**
   * Récupère une facture par son numéro
   */
  async getInvoiceByNumber(invoiceNumber: string): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/number/${invoiceNumber}`);
  }

  /**
   * Crée une nouvelle facture
   */
  async createInvoice(invoice: InvoiceRequest): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>('/invoices', {
      method: 'POST',
      body: JSON.stringify(invoice),
    });
  }

  /**
   * Met à jour une facture existante
   */
  async updateInvoice(id: number, invoice: InvoiceRequest): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/${id}`, {
      method: 'PUT',
      body: JSON.stringify(invoice),
    });
  }

  /**
   * Supprime une facture
   */
  async deleteInvoice(id: number): Promise<void> {
    await fetch(`${API_BASE_URL}/invoices/${id}`, {
      method: 'DELETE',
    });
  }

  /**
   * Soumet une facture à la DGI
   */
  async submitToDgi(id: number): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/${id}/submit-to-dgi`, {
      method: 'POST',
    });
  }

  /**
   * Télécharge une facture en PDF (future fonctionnalité)
   */
  async downloadInvoicePdf(id: number): Promise<Blob> {
    const response = await fetch(`${API_BASE_URL}/invoices/${id}/pdf`);
    if (!response.ok) {
      throw new Error('Erreur lors du téléchargement du PDF');
    }
    return response.blob();
  }
}

export const invoiceService = new InvoiceService();