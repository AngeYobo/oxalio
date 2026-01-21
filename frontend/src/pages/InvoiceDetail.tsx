import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { useInvoice } from '../hooks/useInvoices';
import { invoiceService } from '../services/invoiceService';
import type { InvoiceResponse } from "../types/invoice-types";


export default function InvoiceDetail() {
  const { id = '' } = useParams();
  const { data, isLoading, isError, refetch } = useInvoice(id);
  const [submitting, setSubmitting] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmitToDgi = async () => {
    if (!id) return;
    try {
      setSubmitting(true);
      setError(null);
      await invoiceService.submitToDgi(Number(id));
      await refetch(); // recharge la facture avec dgiReference / qrBase64 à jour
    } catch (e: any) {
      setError(e?.message || 'Erreur lors de la soumission à la DGI (mock)');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!id) return;
    try {
      setDownloading(true);
      setError(null);
      const blob = await invoiceService.downloadInvoicePdf(Number(id));

      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      // Nom de fichier : si on a le numéro de facture on l’utilise, sinon l’ID
      const fileName = data?.invoiceNumber
        ? `facture-${data.invoiceNumber}.pdf`
        : `facture-${id}.pdf`;
      link.download = fileName;

      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (e: any) {
      setError(e?.message || 'Erreur lors du téléchargement du PDF (mock)');
    } finally {
      setDownloading(false);
    }
  };

  if (isLoading) return <p>Chargement…</p>;
  if (isError || !data) return <p className="text-red-600">Introuvable.</p>;

  

  // Compat FNE + legacy (mock)
  const reference = (data as any).fneReference ?? (data as any).dgiReference;
  const qrBase64 = (data as any).fneQrCode ?? (data as any).qrBase64;
  const submittedAt = (data as any).fneCertifiedAt ?? (data as any).dgiSubmittedAt;

  const isSubmitted = Boolean(reference);


  return (
    <div className="p-6 space-y-6 bg-white border rounded-lg">
      {/* En-tête facture + statut + boutons DGI / PDF */}
      <header className="flex items-start justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-xl font-semibold">
            Facture {data.invoiceNumber}
          </h1>
          <div className="text-sm text-gray-600">
            Client : {data?.buyer?.name ?? '—'}
          </div>
          <div className="text-sm text-gray-600">
            Montant TTC : {data?.totals?.totalAmount ?? '—'} {data?.currency}
          </div>
          <div className="text-sm">
            Statut :{' '}
            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono bg-gray-100 text-gray-700">
              {data.status}
            </span>
          </div>
          {isSubmitted && (
            <div className="text-sm text-emerald-700">
              Soumise à la DGI (mock) – Réf :{' '}
              <span className="font-mono break-all">
                {reference}
              </span>
            </div>
          )}
          {error && (
            <p className="text-sm text-red-600">
              {error}
            </p>
          )}
        </div>

        <div className="flex flex-col items-end gap-2">
          <div className="flex gap-2">
            {!isSubmitted && (
              <button
                onClick={handleSubmitToDgi}
                disabled={submitting}
                className="px-4 py-2 rounded bg-indigo-600 text-white text-sm font-medium disabled:opacity-60"
              >
                {submitting ? 'Soumission en cours…' : 'Soumettre à la DGI (mock)'}
              </button>
            )}
            <button
              onClick={handleDownloadPdf}
              disabled={downloading}
              className="px-4 py-2 rounded bg-gray-800 text-white text-sm font-medium disabled:opacity-60"
            >
              {downloading ? 'Génération PDF…' : 'Télécharger le PDF (mock)'}
            </button>
          </div>
          {isSubmitted && (
            <span className="px-3 py-1 text-xs rounded-full bg-emerald-100 text-emerald-700">
              Certifiée FNE – Simulation
            </span>
          )}
        </div>
      </header>

      {/* Bloc QR + debug */}
      <section className="flex flex-wrap gap-6 items-start">
        {qrBase64 && (
          <div className="border rounded-lg p-4 bg-gray-50 shadow-sm">
            <p className="text-sm font-medium mb-2">
              QR FNE (mock)
            </p>
            <img
              src={`data:image/png;base64,${qrBase64}`}
              alt="QR code facture (mock FNE)"
              className="w-40 h-40 object-contain border bg-white"
            />
            {submittedAt && (
              <p className="mt-2 text-xs text-gray-500">
                Soumise le :{' '}
                {new Date(submittedAt).toLocaleString()}
              </p>
            )}
          </div>
        )}

        <div className="flex-1 min-w-[240px]">
          <h2 className="text-sm font-semibold mb-2">Détails bruts (debug)</h2>
          <pre className="text-xs bg-gray-50 p-3 rounded overflow-x-auto max-h-96">
            {JSON.stringify(data, null, 2)}
          </pre>
        </div>
      </section>
    </div>
  );
}
