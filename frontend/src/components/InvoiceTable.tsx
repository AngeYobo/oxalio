import { useInvoices } from '../hooks/useInvoices';
import { Link } from 'react-router-dom';

export default function InvoiceTable() {
  const { data, isLoading, isError } = useInvoices();

  if (isLoading) return <p>Chargement…</p>;
  if (isError) return <p className="text-red-600">Erreur de chargement</p>;
  if (!data?.length) return <p>Aucune facture.</p>;

  return (
    <div className="overflow-x-auto bg-white border rounded-lg">
      <table className="min-w-full">
        <thead>
          <tr className="bg-gray-100 text-left text-sm">
            <th className="p-2">Numéro</th>
            <th className="p-2">Client</th>
            <th className="p-2">Montant</th>
            <th className="p-2">Statut</th>
            <th className="p-2"></th>
          </tr>
        </thead>
        <tbody>
          {data.map((inv) => (
            <tr key={inv.id} className="border-t">
              <td className="p-2">{inv.id}</td>
              <td className="p-2">{inv.client}</td>
              <td className="p-2">{inv.amount} {inv.currency}</td>
              <td className="p-2">{inv.status}</td>
              <td className="p-2">
                <Link to={`/invoice/${encodeURIComponent(inv.id)}`} className="text-blue-600 hover:underline text-sm">
                  Détails
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
