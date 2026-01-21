// src/components/InvoiceList.tsx

import React, { useState, useEffect } from 'react';
import { Search, Plus, FileText, Download, Eye, Trash2, Send } from 'lucide-react';
import { InvoiceResponse } from '../types/invoice-types';
import { invoiceService } from '../services/invoiceService';

const InvoiceList: React.FC = () => {
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadInvoices();
  }, []);

  const loadInvoices = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await invoiceService.getAllInvoices();
      setInvoices(data);
    } catch (err) {
      setError('Erreur lors du chargement des factures');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette facture ?')) return;
    
    try {
      await invoiceService.deleteInvoice(id);
      setInvoices(invoices.filter(inv => inv.id !== id));
    } catch (err) {
      alert('Erreur lors de la suppression');
      console.error(err);
    }
  };

  const handleSubmitToDgi = async (id: number) => {
    try {
      const updated = await invoiceService.submitToDgi(id);
      setInvoices(invoices.map(inv => inv.id === id ? updated : inv));
      alert(`Facture soumise avec succès ! Référence DGI: ${updated.dgiReference}`);
    } catch (err) {
      alert('Erreur lors de la soumission à la DGI');
      console.error(err);
    }
  };

  const needle = searchTerm.toLowerCase();

  const filteredInvoices = invoices.filter(invoice => {
    const matchesSearch =
      (invoice.invoiceNumber ?? '').toLowerCase().includes(needle) ||
      invoice.buyer.name.toLowerCase().includes(needle) ||
      invoice.seller.companyName.toLowerCase().includes(needle);

    const matchesStatus = statusFilter === 'ALL' || invoice.status === statusFilter;

    return matchesSearch && matchesStatus;
  });


  const getStatusBadge = (status: string) => {
    const styles = {
      RECEIVED: 'bg-blue-100 text-blue-800',
      SUBMITTED_TO_DGI: 'bg-green-100 text-green-800',
      REJECTED: 'bg-red-100 text-red-800',
      CANCELLED: 'bg-gray-100 text-gray-800',
    };
    return styles[status as keyof typeof styles] || 'bg-gray-100 text-gray-800';
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'decimal',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount) + ' ' + currency;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Factures</h1>
          <p className="text-gray-600 mt-1">Gestion des factures conformes DGI</p>
        </div>
        <button
          onClick={() => window.location.href = '/invoices/new'}
          className="flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors shadow-lg"
        >
          <Plus size={20} />
          Nouvelle facture
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow-md">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Rechercher par numéro, vendeur ou acheteur..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="ALL">Tous les statuts</option>
            <option value="RECEIVED">Reçue</option>
            <option value="SUBMITTED_TO_DGI">Soumise DGI</option>
            <option value="REJECTED">Rejetée</option>
            <option value="CANCELLED">Annulée</option>
          </select>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="text-sm text-gray-600">Total factures</div>
          <div className="text-2xl font-bold text-gray-900 mt-1">{invoices.length}</div>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="text-sm text-gray-600">Soumises DGI</div>
          <div className="text-2xl font-bold text-green-600 mt-1">
            {invoices.filter(i => i.status === 'SUBMITTED_TO_DGI').length}
          </div>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="text-sm text-gray-600">En attente</div>
          <div className="text-2xl font-bold text-blue-600 mt-1">
            {invoices.filter(i => i.status === 'RECEIVED').length}
          </div>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="text-sm text-gray-600">Montant total</div>
          <div className="text-2xl font-bold text-gray-900 mt-1">
            {formatAmount(
              invoices.reduce((sum, inv) => sum + inv.totals.totalAmount, 0),
              'XOF'
            )}
          </div>
        </div>
      </div>

      {/* Invoice Table */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Numéro
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Date
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Vendeur
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acheteur
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Montant
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Statut
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredInvoices.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-gray-500">
                    <FileText className="mx-auto mb-2" size={48} />
                    <p>Aucune facture trouvée</p>
                  </td>
                </tr>
              ) : (
                filteredInvoices.map((invoice) => (
                  <tr key={invoice.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <FileText className="text-blue-600 mr-2" size={18} />
                        <span className="font-medium text-gray-900">{invoice.invoiceNumber}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {formatDate(invoice.issueDate)}
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900">{invoice.seller.companyName}</div>
                      <div className="text-xs text-gray-500">{invoice.seller.taxId}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900">{invoice.buyer.name}</div>
                      <div className="text-xs text-gray-500">{invoice.buyer.taxId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-semibold text-gray-900">
                        {formatAmount(invoice.totals.totalAmount, invoice.currency)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadge(invoice.status)}`}>
                        {invoice.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => window.location.href = `/invoices/${invoice.id}`}
                          className="text-blue-600 hover:text-blue-900 p-1"
                          title="Voir détails"
                        >
                          <Eye size={18} />
                        </button>
                        {invoice.status === 'RECEIVED' && (
                          <button
                            onClick={() => handleSubmitToDgi(invoice.id)}
                            className="text-green-600 hover:text-green-900 p-1"
                            title="Soumettre à la DGI"
                          >
                            <Send size={18} />
                          </button>
                        )}
                        <button
                          onClick={() => handleDelete(invoice.id)}
                          className="text-red-600 hover:text-red-900 p-1"
                          title="Supprimer"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default InvoiceList;