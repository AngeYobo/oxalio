import axios from 'axios'

// Base API URL
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://8080-sidereal-election-ozhudl.us1.demeter.run/api/v1',
  timeout: 10000,
})

export type InvoiceResponse = {
  id: number
  invoiceNumber: string
  currency: string
  status: string
  buyer: { name: string }
  totals: { totalAmount: number }
}

export type InvoiceListItem = {
  id: string
  client: string
  amount: number
  currency: string
  status: string
  raw: InvoiceResponse
}

// ✅ Charger la vraie liste depuis ton backend
export async function getInvoices(): Promise<InvoiceListItem[]> {
  const r = await api.get<InvoiceResponse[]>('/invoices')  // 👈 route réelle
  return r.data.map((inv) => ({
    id: inv.id.toString(),
    client: inv.buyer?.name ?? '—',
    amount: inv.totals?.totalAmount ?? 0,
    currency: inv.currency ?? 'XOF',
    status: inv.status ?? 'N/A',
    raw: inv,
  }))
}

// ✅ Charger une facture précise
export async function getInvoiceById(id: string): Promise<InvoiceResponse> {
  const r = await api.get<InvoiceResponse>(`/invoices/${id}`)  // 👈 route réelle
  return r.data
}
