import React, { useEffect, useState } from 'react'
import { api } from '../api/client'

export const Invoices: React.FC = () => {
  const [rows,setRows] = useState<any[]>([])
  const [loading,setLoading] = useState(true)

  useEffect(()=>{
    api.get('/invoices').then(r=> setRows(r.data)).finally(()=> setLoading(false))
  },[])

  if (loading) return <div style={{padding:24}}>Chargement…</div>

  return (
    <div style={{padding:24}}>
      <h2>Factures</h2>
      <table border={1} cellPadding={6}>
        <thead><tr><th>ID</th><th>Statut</th></tr></thead>
        <tbody>
          {rows.map((r,i)=>(<tr key={i}><td>{r.id}</td><td>{r.status}</td></tr>))}
        </tbody>
      </table>
    </div>
  )
}
