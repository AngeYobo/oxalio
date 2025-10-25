import axios from 'axios'

/** Base API URL:
 * - Dev: from .env.development -> VITE_API_URL=http://localhost:8080
 * - Prod: from .env.production  -> VITE_API_URL=https://8080-...demeter.run
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

/* ---- Minimal types for the demo ---- */
type DemoInvoice = {
  invoiceNumber: string
  currency: string
  status?: string
  buyer?: { name?: string }
  totals?: { totalAmount?: number }
}

export type InvoiceListItem = {
  id: string
  client: string
  amount: number
  currency: string
  status: string
  raw: DemoInvoice
}

/** The backend exposes /invoices/demo (single invoice).
 * We synthesize a small list on the frontend for the demo UI.
 */
export async function getInvoices(): Promise<InvoiceListItem[]> {
  const r = await api.get<DemoInvoice>('/invoices/demo')
  const one = r.data

  return Array.from({ length: 5 }).map((_, i) => ({
    id: `${one.invoiceNumber}-${i + 1}`,
    client: one?.buyer?.name ?? 'Client Démo',
    amount: one?.totals?.totalAmount ?? 11800,
    currency: one?.currency ?? 'XOF',
    status: one?.status ?? 'MOCK_READY',
    raw: one,
  }))
}

export async function getInvoiceById(id: string): Promise<DemoInvoice> {
  // In real life: GET /invoices/{id}
  // Here: reuse the demo invoice and override the id
  const r = await api.get<DemoInvoice>('/invoices/demo')
  return { ...r.data, invoiceNumber: id }
}
