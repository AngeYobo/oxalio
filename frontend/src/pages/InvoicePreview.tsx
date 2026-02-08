import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api/client';

export default function InvoicePreview() {
  const { id } = useParams<{ id: string }>();
  const [invoice, setInvoice] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadInvoice();
  }, [id]);

  const loadInvoice = async () => {
    try {
      const response = await api.get(`/invoices/${id}`);
      setInvoice(response.data);
    } catch (error) {
      console.error('Erreur chargement facture:', error);
    } finally {
      setLoading(false);
    }
  };

  const downloadPDF = async () => {
    try {
      const response = await api.get(`/invoices/${id}/pdf`, {
        responseType: 'blob',
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `facture-${invoice.invoiceNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Erreur téléchargement PDF:', error);
    }
  };

  if (loading) {
    return <div className="p-8">Chargement...</div>;
  }

  if (!invoice) {
    return <div className="p-8">Facture introuvable</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-lg p-8 mb-4">
          <div className="text-center border-b-4 border-blue-600 pb-4 mb-6">
            <h1 className="text-3xl font-bold text-blue-600">
              REÇU NORMALISÉ ÉLECTRONIQUE (RNE)
            </h1>
            <p className="text-gray-600">République de Côte d'Ivoire</p>
          </div>

          {/* Seller & Buyer */}
          <div className="grid grid-cols-2 gap-6 mb-6">
            <div className="border border-gray-300 rounded-lg p-4">
              <h3 className="font-bold text-blue-600 mb-2">🏢 VENDEUR</h3>
              <p className="font-bold">{invoice.seller.companyName}</p>
              <p>{invoice.seller.address}</p>
              <p>NCC : {invoice.seller.taxId}</p>
            </div>

            <div className="border border-gray-300 rounded-lg p-4">
              <h3 className="font-bold text-blue-600 mb-2">👤 ACHETEUR</h3>
              <p className="font-bold">{invoice.buyer.name}</p>
              <p>{invoice.buyer.address}</p>
              <p>NCC : {invoice.buyer.taxId}</p>
            </div>
          </div>

          {/* Invoice Details */}
          <div className="bg-gray-100 rounded-lg p-4 mb-6">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p><span className="font-semibold">N° Facture :</span> {invoice.invoiceNumber}</p>
                <p><span className="font-semibold">Date :</span> {new Date(invoice.issueDate).toLocaleDateString('fr-FR')}</p>
              </div>
              <div>
                <p><span className="font-semibold">Sticker ID :</span> {invoice.stickerId}</p>
                <p><span className="font-semibold">Statut :</span> 
                  <span className={`ml-2 px-2 py-1 rounded text-sm ${
                    invoice.status === 'CERTIFIED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                  }`}>
                    {invoice.status}
                  </span>
                </p>
              </div>
            </div>
          </div>

          {/* Items Table */}
          <table className="w-full mb-6">
            <thead className="bg-blue-600 text-white">
              <tr>
                <th className="p-3 text-left">Désignation</th>
                <th className="p-3 text-center">Qté</th>
                <th className="p-3 text-right">PU HT</th>
                <th className="p-3 text-right">TVA</th>
                <th className="p-3 text-right">Total TTC</th>
              </tr>
            </thead>
            <tbody>
              {invoice.lines.map((line: any, index: number) => (
                <tr key={index} className="border-b">
                  <td className="p-3">{line.description}</td>
                  <td className="p-3 text-center">{line.quantity}</td>
                  <td className="p-3 text-right">{line.unitPrice} FCFA</td>
                  <td className="p-3 text-right">{line.vatAmount} FCFA</td>
                  <td className="p-3 text-right font-semibold">{line.lineTotal} FCFA</td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Totals */}
          <div className="text-right mb-6">
            <p className="text-lg">Sous-total HT : <span className="font-semibold">{invoice.totals.subtotal} FCFA</span></p>
            <p className="text-lg">TVA : <span className="font-semibold">{invoice.totals.totalVat} FCFA</span></p>
            <hr className="my-2" />
            <p className="text-2xl font-bold text-blue-600">
              TOTAL À PAYER : {invoice.totals.totalToPay} FCFA
            </p>
            <p className="text-gray-600">Mode de paiement : {invoice.paymentMode}</p>
          </div>

          {/* QR Code */}
          {invoice.qrBase64 && (
            <div className="text-center border-t-2 border-dashed border-gray-300 pt-6">
              <p className="font-semibold mb-2">Scanner pour vérifier l'authenticité</p>
              <img
                src={`data:image/png;base64,${invoice.qrBase64}`}
                alt="QR Code"
                className="w-48 h-48 mx-auto"
              />
              <p className="text-sm text-gray-600 mt-2">Sticker ID : {invoice.stickerId}</p>
            </div>
          )}

          {/* Footer */}
          <div className="text-center text-gray-600 text-sm mt-6 pt-6 border-t border-gray-300">
            <p className="font-semibold">Merci de votre confiance !</p>
            <p>Ce document est un reçu électronique certifié par la Direction Générale des Impôts (DGI)</p>
            <p className="mt-2">OXALIO SARL - support@oxalio.ci - www.oxalio.ci</p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-4 justify-center">
          <button
            onClick={downloadPDF}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
          >
            📄 Télécharger PDF
          </button>
          <button
            onClick={() => window.print()}
            className="px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 flex items-center gap-2"
          >
            🖨️ Imprimer
          </button>
        </div>
      </div>
    </div>
  );
}