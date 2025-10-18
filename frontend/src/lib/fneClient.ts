import axios, { AxiosInstance } from 'axios'

export interface Party {
  taxId?: string
  companyName?: string
  name?: string
  address?: string
}
export interface Line {
  description: string
  quantity: number
  unitPrice: number
  vatRate?: number
  vatAmount?: number
  discount?: number
}
export interface Totals {
  subtotal: number
  totalVat: number
  totalAmount: number
}
export interface InvoiceSubmit {
  invoiceNumber: string
  issueDate: string // ISO
  currency: string  // XOF, EUR…
  invoiceType?: 'STANDARD'|'CREDIT_NOTE'|'PROFORMA'
  paymentMode?: 'CASH'|'CARD'|'TRANSFER'|'MOBILE'
  seller: Party
  buyer: Party
  lines: Line[]
  totals: Totals
  metadata?: Record<string, unknown>
  notify?: { webhookUrl?: string }
}
export type InvoiceStatus =
  | 'RECEIVED'|'VALIDATING'|'ACCEPTED'|'SIGNED'|'REJECTED'|'CANCELLED'|'CREDITED'

export interface InvoiceSigned extends InvoiceSubmit {
  reference: string
  status: InvoiceStatus
  signature?: string
  qrCode?: string
  hash?: string
  processedAt?: string
}

export interface ErrorResponse {
  timestamp: string
  httpStatus: number
  code: string
  message: string
  details?: { field?: string; issue?: string }[]
  correlationId?: string
}

export class FneClient {
  private http: AxiosInstance

  constructor(opts: { baseURL: string; token?: string; timeout?: number }) {
    this.http = axios.create({
      baseURL: opts.baseURL,
      timeout: opts.timeout ?? 15000,
      headers: opts.token ? { Authorization: `Bearer ${opts.token}` } : undefined,
    })
  }

  async submitInvoice(body: InvoiceSubmit, idempotencyKey: string) {
    const r = await this.http.post<InvoiceSigned | {
      reference: string; status: string; message?: string; links?: Record<string, string>
    }>('/invoices', body, { headers: { 'Idempotency-Key': idempotencyKey } })
    return r.data
  }

  async getInvoice(reference: string) {
    const r = await this.http.get<InvoiceSigned>(`/invoices/${encodeURIComponent(reference)}`)
    return r.data
  }

  async listInvoices(params?: {
    status?: InvoiceStatus; from?: string; to?: string; page?: number; size?: number; sort?: string
  }) {
    const r = await this.http.get<{
      page: number; size: number; totalElements: number; content: Array<{
        reference: string; invoiceNumber: string; status: InvoiceStatus; totalAmount: number; currency: string; issueDate: string
      }>
    }>('/invoices', { params })
    return r.data
  }

  async cancel(reference: string, reason: { reasonCode?: string; reason?: string }, idempotencyKey: string) {
    const r = await this.http.post(`/invoices/${encodeURIComponent(reference)}/cancel`, reason, {
      headers: { 'Idempotency-Key': idempotencyKey },
    })
    return r.data as { reference: string; status: 'CANCELLED'; cancelledAt: string }
  }

  async creditNote(reference: string, body: InvoiceSubmit, idempotencyKey: string) {
    const r = await this.http.post<InvoiceSigned>(
      `/invoices/${encodeURIComponent(reference)}/credit-note`, body,
      { headers: { 'Idempotency-Key': idempotencyKey } },
    )
    return r.data
  }
}
