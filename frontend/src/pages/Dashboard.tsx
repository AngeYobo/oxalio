import InvoiceTable from '../components/InvoiceTable';

export default function Dashboard() {
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Factures</h1>
      <InvoiceTable />
    </div>
  );
}
