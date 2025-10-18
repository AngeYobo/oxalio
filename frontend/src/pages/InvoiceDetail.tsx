import { useParams } from 'react-router-dom';
import { useInvoice } from '../hooks/useInvoices';

export default function InvoiceDetail() {
  const { id = '' } = useParams();
  const { data, isLoading, isError } = useInvoice(id);

  if (isLoading) return <p>Chargement…</p>;
  if (isError || !data) return <p className="text-red-600">Introuvable.</p>;

  return (
    <div className="bg-white border rounded-lg p-4 space-y-2">
      <h1 className="text-xl font-semibold">Facture {data.invoiceNumber}</h1>
      <div className="text-sm text-gray-600">Client: {data?.buyer?.name}</div>
      <div className="text-sm text-gray-600">Montant TTC: {data?.totals?.totalAmount} {data?.currency}</div>
      <pre className="text-xs bg-gray-50 p-3 rounded overflow-x-auto">
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
}
