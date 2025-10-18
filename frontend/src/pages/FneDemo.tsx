import { useState, useEffect } from 'react'
import { v4 as uuid } from 'uuid'
import { fne } from '../lib/fne'

interface InvoiceSummary {
  reference: string
  invoiceNumber: string
  totalAmount: number
  currency: string
  status: string
  issueDate: string
}

export default function FneDemo() {
  const [invoices, setInvoices] = useState<InvoiceSummary[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<any>(null)

  // Charger la liste des factures signées au montage
  useEffect(() => {
    refresh()
  }, [])

  async function refresh() {
    setLoading(true)
    setError(null)
    try {
      const list = await fne.listInvoices({ status: 'SIGNED', page: 1, size: 10 })
      setInvoices(list.content)
    } catch (err: any) {
      setError('Impossible de charger la liste des factures')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  async function sendDemo() {
    setLoading(true)
    setError(null)
    try {
      const payload = {
        invoiceNumber: `INV-${new Date().getTime()}`,
        issueDate: new Date().toISOString(),
        currency: 'XOF',
        seller: { taxId: 'NIF123456A', companyName: 'Oxalio SARL', address: 'Abidjan' },
        buyer: { name: 'Client Démo', address: 'Bouaké' },
        lines: [{ description: 'Prestation', quantity: 1, unitPrice: 10000, vatRate: 18 }],
        totals: { subtotal: 10000, totalVat: 1800, totalAmount: 11800 },
      }
      const res = await fne.submitInvoice(payload, uuid())
      console.log('Facture envoyée:', res)
      await refresh()
    } catch (err: any) {
      setError('Erreur lors de l’envoi de la facture')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  async function viewInvoice(ref: string) {
    setLoading(true)
    setError(null)
    try {
      const data = await fne.getInvoice(ref)
      setSelected(data)
    } catch (err: any) {
      setError('Erreur lors de la récupération de la facture')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-4">
      <h1 className="text-2xl font-bold mb-4">📄 FNE Demo — Oxalio Front</h1>

      <div className="flex space-x-2">
        <button
          onClick={sendDemo}
          disabled={loading}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
        >
          ➕ Envoyer une facture de démo
        </button>
        <button
          onClick={refresh}
          disabled={loading}
          className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 disabled:bg-gray-400"
        >
          🔄 Rafraîchir
        </button>
      </div>

      {error && <div className="text-red-600">{error}</div>}

      {loading && <div>⏳ Chargement…</div>}

      {!loading && invoices.length === 0 && (
        <div className="text-gray-500">Aucune facture trouvée.</div>
      )}

      {!loading && invoices.length > 0 && (
        <table className="w-full border mt-4">
          <thead>
            <tr className="bg-gray-100 text-left">
              <th className="p-2">#</th>
              <th className="p-2">Montant</th>
              <th className="p-2">Statut</th>
              <th className="p-2">Date</th>
              <th className="p-2"></th>
            </tr>
          </thead>
          <tbody>
            {invoices.map((inv) => (
              <tr key={inv.reference} className="border-t hover:bg-gray-50">
                <td className="p-2">{inv.invoiceNumber}</td>
                <td className="p-2">{inv.totalAmount} {inv.currency}</td>
                <td className="p-2">{inv.status}</td>
                <td className="p-2">
                  {new Date(inv.issueDate).toLocaleString()}
                </td>
                <td className="p-2">
                  <button
                    onClick={() => viewInvoice(inv.reference)}
                    className="text-blue-600 hover:underline"
                  >
                    Voir
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {selected && (
        <div className="mt-6 border rounded p-4 bg-gray-50">
          <h2 className="text-lg font-bold mb-2">
            Facture {selected.invoiceNumber}
          </h2>
          <p>Statut : {selected.status}</p>
          <p>Montant : {selected.totals.totalAmount} {selected.currency}</p>
          <p>Vendeur : {selected.seller?.companyName}</p>
          <p>Acheteur : {selected.buyer?.name}</p>
          {selected.qrCode && (
            <div className="mt-4">
              <img
                src={selected.qrCode}
                alt="QR code"
                className="border inline-block"
              />
            </div>
          )}
        </div>
      )}
    </div>
  )
}
