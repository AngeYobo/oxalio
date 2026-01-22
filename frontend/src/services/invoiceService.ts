// src/services/invoiceService.ts
import { apiClient } from '../api/client';
import type { InvoiceRequest, InvoiceResponse } from '../types/invoice-types';

class InvoiceService {
  async getAllInvoices(): Promise<InvoiceResponse[]> {
    const res = await apiClient.get<InvoiceResponse[]>('/invoices');
    return res.data;
  }

  async getInvoiceById(id: number): Promise<InvoiceResponse> {
    const res = await apiClient.get<InvoiceResponse>(`/invoices/${id}`);
    return res.data;
  }

  async getInvoiceByNumber(invoiceNumber: string): Promise<InvoiceResponse> {
    const res = await apiClient.get<InvoiceResponse>(`/invoices/number/${invoiceNumber}`);
    return res.data;
  }

  async createInvoice(invoice: InvoiceRequest): Promise<InvoiceResponse> {
    const res = await apiClient.post<InvoiceResponse>('/invoices', invoice);
    return res.data;
  }

  async updateInvoice(id: number, invoice: InvoiceRequest): Promise<InvoiceResponse> {
    const res = await apiClient.put<InvoiceResponse>(`/invoices/${id}`, invoice);
    return res.data;
  }

  async deleteInvoice(id: number): Promise<void> {
    await apiClient.delete(`/invoices/${id}`);
  }

  async submitToDgi(id: number): Promise<InvoiceResponse> {
    const res = await apiClient.post<InvoiceResponse>(`/invoices/${id}/submit-to-dgi`);
    return res.data;
  }

  async downloadInvoicePdf(id: number): Promise<Blob> {
    const res = await apiClient.get(`/invoices/${id}/pdf`, {
      responseType: 'blob',
    });
    return res.data as Blob;
  }
}

export const invoiceService = new InvoiceService();
