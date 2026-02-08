import { useState } from 'react';
import { api } from '../api/client';
import type { InvoiceResponse } from '../types/invoice-types';

export default function CreateInvoicePage() {
  const [loading, setLoading] = useState(false);
  const [invoice, setInvoice] = useState<InvoiceResponse | null>(null); // ← ICI
  const [error, setError] = useState<string | null>(null);

  const createInvoice = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await api.post('/invoices', {
        invoiceType: 'STANDARD',
        paymentMode: 'CASH',
        currency: 'XOF',
        template: 'B2C',
        isRne: false,
        seller: {
          taxId: 'CI0250584',
          companyName: 'OXALIO SARL',
          address: 'Bouaké',
          email: 'contact@oxalio.ci',
          phone: '+225 07 XX XX XX XX',
        },
        buyer: {
          taxId: 'CI9999999',
          name: 'Client Test',
          address: 'Abidjan',
          email: 'client@test.ci',
          phone: '+225 07 YY YY YY YY',
        },
        lines: [
          {
            description: 'Produit Test',
            quantity: 1,
            unitPrice: 10000,
            vatRate: 18,
            vatAmount: 1800,
            discount: 0,
            totalAmount: 11800,
          },
        ],
        totals: {
          subtotal: 10000,
          totalVat: 1800,
          totalTax: 1800,
          totalAmount: 11800,
          totalToPay: 11800,
        },
      });

      setInvoice(response.data);
    } catch (error: any) {
      console.error('Erreur:', error);
      setError(error.response?.data?.message || 'Erreur lors de la création');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">
          Créer une facture
        </h1>

        {/* Bouton de création */}
        <button
          onClick={createInvoice}
          disabled={loading}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition flex items-center gap-2"
        >
          {loading ? (
            <>
              <span className="animate-spin">⏳</span>
              Création en cours...
            </>
          ) : (
            <>
              📝 Créer facture test
            </>
          )}
        </button>

        {/* Erreur */}
        {error && (
          <div className="mt-4 bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">❌ {error}</p>
          </div>
        )}

        {/* Résultat */}
        {invoice && (
          <div className="mt-6 bg-white rounded-lg shadow-lg overflow-hidden">
            {/* Header */}
            <div className="bg-gradient-to-r from-blue-600 to-blue-700 text-white p-6">
              <div className="flex justify-between items-start">
                <div>
                  <h2 className="text-2xl font-bold mb-2">
                    {invoice.invoiceNumber || 'Sans numéro'}
                  </h2>
                  <p className="text-blue-100">
                    Créée le {new Date(invoice.issueDate || invoice.createdAt).toLocaleDateString('fr-FR')}
                  </p>
                </div>
                <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
                  invoice.status === 'CERTIFIED' 
                    ? 'bg-green-500 text-white'
                    : 'bg-yellow-400 text-gray-900'
                }`}>
                  {invoice.status}
                </span>
              </div>
            </div>

            <div className="p-6">
              {/* Infos principales */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                  <p className="text-sm text-gray-600 mb-1">Statut</p>
                  <p className="text-lg font-semibold text-gray-900">
                    {invoice.status}
                  </p>
                </div>

                <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
                  <p className="text-sm text-gray-600 mb-1">Montant total</p>
                  <p className="text-2xl font-bold text-blue-600">
                    {invoice.totals?.totalAmount?.toLocaleString() || invoice.totalAmount?.toLocaleString()} FCFA
                  </p>
                </div>

                <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                  <p className="text-sm text-gray-600 mb-1">Mode de paiement</p>
                  <p className="text-lg font-semibold text-gray-900">
                    {invoice.paymentMode || invoice.paymentMethod}
                  </p>
                </div>
              </div>

              {/* Vendeur / Acheteur */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                <div className="border border-gray-200 rounded-lg p-4">
                  <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                    <span className="text-2xl">🏢</span>
                    Vendeur
                  </h3>
                  <p className="font-semibold text-gray-900">{invoice.seller.companyName}</p>
                  <p className="text-sm text-gray-600">{invoice.seller.address}</p>
                  <p className="text-sm text-gray-600">NCC: {invoice.seller.taxId}</p>
                  {invoice.seller.email && (
                    <p className="text-sm text-gray-600">✉️ {invoice.seller.email}</p>
                  )}
                  {invoice.seller.phone && (
                    <p className="text-sm text-gray-600">📞 {invoice.seller.phone}</p>
                  )}
                </div>

                <div className="border border-gray-200 rounded-lg p-4">
                  <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                    <span className="text-2xl">👤</span>
                    Acheteur
                  </h3>
                  <p className="font-semibold text-gray-900">{invoice.buyer.name}</p>
                  <p className="text-sm text-gray-600">{invoice.buyer.address}</p>
                  <p className="text-sm text-gray-600">NCC: {invoice.buyer.taxId}</p>
                  {invoice.buyer.email && (
                    <p className="text-sm text-gray-600">✉️ {invoice.buyer.email}</p>
                  )}
                  {invoice.buyer.phone && (
                    <p className="text-sm text-gray-600">📞 {invoice.buyer.phone}</p>
                  )}
                </div>
              </div>

              {/* Articles */}
              {(invoice.lines || invoice.items) && (
                <div className="mb-6">
                  <h3 className="font-semibold text-gray-900 mb-3 text-lg">
                    📦 Articles
                  </h3>
                  <div className="overflow-x-auto">
                    <table className="w-full border-collapse">
                      <thead>
                        <tr className="bg-gray-100 border-b-2 border-gray-300">
                          <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Description</th>
                          <th className="px-4 py-3 text-center text-sm font-semibold text-gray-700">Qté</th>
                          <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700">PU HT</th>
                          <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700">TVA</th>
                          <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700">Total</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-200">
                        {(invoice.lines || invoice.items || []).map((line: any, index: number) => (
                          <tr key={index} className="hover:bg-gray-50">
                            <td className="px-4 py-3 text-sm">{line.description}</td>
                            <td className="px-4 py-3 text-center text-sm">{line.quantity}</td>
                            <td className="px-4 py-3 text-right text-sm">
                              {line.unitPrice?.toLocaleString()} FCFA
                            </td>
                            <td className="px-4 py-3 text-right text-sm">
                              {line.vatAmount?.toLocaleString()} FCFA
                            </td>
                            <td className="px-4 py-3 text-right text-sm font-semibold">
                              {(line.lineTotal || line.totalAmount)?.toLocaleString()} FCFA
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Totaux */}
              <div className="bg-gray-50 rounded-lg p-6 border-2 border-gray-200">
                <div className="text-right space-y-2">
                  <p className="text-sm text-gray-600">
                    Sous-total HT:{' '}
                    <span className="font-semibold text-gray-900 ml-2">
                      {(invoice.totals?.subtotal || invoice.subtotal)?.toLocaleString()} FCFA
                    </span>
                  </p>
                  <p className="text-sm text-gray-600">
                    TVA (18%):{' '}
                    <span className="font-semibold text-gray-900 ml-2">
                      {(invoice.totals?.totalVat || invoice.taxAmount)?.toLocaleString()} FCFA
                    </span>
                  </p>
                  <hr className="my-3 border-gray-300" />
                  <p className="text-2xl font-bold text-blue-600">
                    Total TTC: {(invoice.totals?.totalAmount || invoice.totalAmount)?.toLocaleString()} FCFA
                  </p>
                </div>
              </div>

              {/* QR Code */}
              {(invoice.qrBase64 || invoice.fneQrCode) && (
                <div className="mt-6 border-t-2 border-dashed border-gray-300 pt-6">
                  <h3 className="font-semibold text-gray-900 mb-3 text-center text-lg">
                    🔍 QR Code de vérification
                  </h3>
                  <div className="flex justify-center">
                    <div className="bg-white p-4 rounded-lg border-2 border-gray-300 shadow-sm">
                      <img
                        src={`data:image/png;base64,${invoice.qrBase64 || invoice.fneQrCode}`}
                        alt="QR Code"
                        className="w-64 h-64"
                      />
                    </div>
                  </div>
                  <p className="text-center text-sm text-gray-600 mt-3">
                    Scanner pour vérifier l'authenticité de la facture
                  </p>
                </div>
              )}

              {/* Actions */}
              <div className="mt-6 pt-6 border-t border-gray-200 flex flex-wrap gap-3 justify-center">
                <button
                  onClick={() => window.location.href = `/invoices/${invoice.id}`}
                  className="px-5 py-2.5 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition flex items-center gap-2"
                >
                  👁️ Voir détails
                </button>
                <button
                  onClick={() => window.print()}
                  className="px-5 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2"
                >
                  🖨️ Imprimer
                </button>
                <button
                  onClick={() => {
                    // TODO: Implémenter téléchargement PDF
                    alert('Téléchargement PDF à implémenter');
                  }}
                  className="px-5 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition flex items-center gap-2"
                >
                  📄 Télécharger PDF
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}