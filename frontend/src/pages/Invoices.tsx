// frontend/src/pages/invoices.tsx
import React, { useEffect, useState } from 'react'
import { api } from '../api/client'

type InvoiceRow = {
  id: string
  status: string
}

// ✅ CHANGER: Retirer 'export' et ajouter 'export default' à la fin
const Invoices: React.FC = () => {
  const [rows, setRows] = useState<InvoiceRow[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.get('/invoices/demo')
      .then(r => {
        const data = r.data
        // ⚠️ On transforme la réponse unique en une liste
        const list: InvoiceRow[] = [
          {
            id: data.invoiceNumber ?? 'INV-UNKNOWN',
            status: data.status ?? 'N/A'
          }
        ]
        setRows(list)
      })
      .catch(err => {
        console.error('Erreur lors du chargement des factures:', err)
        setError("Impossible de charger les factures pour le moment.")
      })
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ padding: 24 }}>Chargement…</div>
  }

  if (error) {
    return <div style={{ padding: 24, color: 'red' }}>{error}</div>
  }

  return (
    <div style={{ padding: 24 }}>
      <h2>Factures</h2>
      {rows.length === 0 ? (
        <div>Aucune facture disponible.</div>
      ) : (
        <table border={1} cellPadding={6}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r, i) => (
              <tr key={i}>
                <td>{r.id}</td>
                <td>{r.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default Invoices;