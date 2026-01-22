import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2, Save } from 'lucide-react';
import { InvoiceRequest, InvoiceLineDTO, TaxType } from '../types/invoice-types';
import { invoiceService } from '../services/invoiceService';

// Lis les infos vendeur depuis env ou fallback
const SELLER_TAX_ID  = import.meta.env.VITE_SELLER_TAX_ID  ?? "CI00000000";
const SELLER_NAME    = import.meta.env.VITE_SELLER_NAME    ?? "Oxalio SARL";
const SELLER_ADDRESS = import.meta.env.VITE_SELLER_ADDRESS ?? "Bouaké, Côte d’Ivoire";
const SELLER_EMAIL   = import.meta.env.VITE_SELLER_EMAIL   ?? ""; // ✅ string
const SELLER_PHONE   = import.meta.env.VITE_SELLER_PHONE   ?? ""; // ✅ string


const CreateInvoice: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const [formData, setFormData] = useState<InvoiceRequest>({
    clientName: '',
    invoiceType: 'STANDARD',
    template: 'B2C',
    paymentMethod: 'TRANSFER',
    isRne: false,
    currency: 'XOF',
    seller: {
      taxId: SELLER_TAX_ID,
      companyName: SELLER_NAME,
      address: SELLER_ADDRESS,
      email: SELLER_EMAIL,
      phone: SELLER_PHONE
    },
    buyer: {
      taxId: '',
      name: '',
      address: '',
      email: '',
      phone: ''
    },
    lines: [{
      description: '',
      quantity: 1,
      unitPrice: 0,
      taxType: TaxType.TVA,
      vatRate: 18,
      vatAmount: 0,
      discount: 0,
      productCode: ''
    }],
    totals: {
      subtotal: 0,
      totalVat: 0,
      totalAmount: 0,
      totalDiscount: 0
    },
    paymentMode: 'TRANSFER',
    notes: ''
  });


  const calculateLineTotals = (lines: InvoiceLineDTO[]) => {
    let subtotal = 0;
    let totalVat = 0;
    let totalDiscount = 0;

    lines.forEach(line => {
      const lineSubtotal = line.quantity * line.unitPrice;
      const lineVat = (lineSubtotal - line.discount) * (line.vatRate / 100);
      
      subtotal += lineSubtotal;
      totalVat += lineVat;
      totalDiscount += line.discount;
      
      line.vatAmount = lineVat;
    });

    const totalAmount = subtotal - totalDiscount + totalVat;

    setFormData(prev => ({
      ...prev,
      lines,
      totals: {
        subtotal,
        totalVat,
        totalAmount,
        totalDiscount
      }
    }));
  };

  const handleAddLine = () => {
    const newLine: InvoiceLineDTO = {
      description: '',
      quantity: 1,
      unitPrice: 0,
      taxType: TaxType.TVA,
      vatRate: 18,
      vatAmount: 0,
      discount: 0,
      productCode: ''
    };
    calculateLineTotals([...formData.lines, newLine]);
  };

  const handleRemoveLine = (index: number) => {
    if (formData.lines.length === 1) {
      alert('Vous devez avoir au moins une ligne de facture');
      return;
    }
    const newLines = formData.lines.filter((_, i) => i !== index);
    calculateLineTotals(newLines);
  };

  const handleLineChange = (index: number, field: keyof InvoiceLineDTO, value: any) => {
    const newLines = [...formData.lines];
    newLines[index] = { ...newLines[index], [field]: value };
    calculateLineTotals(newLines);
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Validation vendeur
    if (!formData.seller.taxId.match(/^CI[0-9]{7,10}$/)) {
      newErrors['seller.taxId'] = 'Format invalide (ex: CI1234567)';
    }
    if (!formData.seller.companyName) {
      newErrors['seller.companyName'] = 'Nom de l\'entreprise requis';
    }
    if (!formData.seller.address) {
      newErrors['seller.address'] = 'Adresse requise';
    }

    // Validation acheteur
    if (!formData.buyer.taxId.match(/^CI[0-9]{7,10}$/)) {
      newErrors['buyer.taxId'] = 'Format invalide (ex: CI7654321)';
    }
    if (!formData.buyer.name) {
      newErrors['buyer.name'] = 'Nom de l\'acheteur requis';
    }
    if (!formData.buyer.address) {
      newErrors['buyer.address'] = 'Adresse requise';
    }

    // Validation lignes
    formData.lines.forEach((line, index) => {
      if (!line.description) {
        newErrors[`lines[${index}].description`] = 'Description requise';
      }
      if (line.quantity <= 0) {
        newErrors[`lines[${index}].quantity`] = 'Quantité doit être > 0';
      }
      if (line.unitPrice < 0) {
        newErrors[`lines[${index}].unitPrice`] = 'Prix unitaire invalide';
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      alert('Veuillez corriger les erreurs du formulaire');
      return;
    }

    setLoading(true);
    try {
      const createdInvoice = await invoiceService.createInvoice(formData);
      alert(`Facture créée avec succès ! Numéro: ${createdInvoice.invoiceNumber}`);
      navigate(`/invoices/${createdInvoice.id}`);
    } catch (error: any) {
      alert('Erreur lors de la création: ' + (error.message || 'Erreur inconnue'));
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <button
          onClick={() => navigate('/invoices')}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft size={20} />
          Retour à la liste
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Nouvelle facture</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* General Info */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Informations générales</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Type de facture
              </label>
              <select
                value={formData.invoiceType}
                onChange={(e) => setFormData({ ...formData, invoiceType: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="STANDARD">Standard</option>
                <option value="PROFORMA">Proforma</option>
                <option value="CREDIT_NOTE">Note de crédit</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Devise
              </label>
              <select
                value={formData.currency}
                onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="XOF">XOF - Franc CFA</option>
                <option value="EUR">EUR - Euro</option>
                <option value="USD">USD - Dollar</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Mode de paiement
              </label>
              <select
                value={formData.paymentMode}
                onChange={(e) => setFormData({ ...formData, paymentMode: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="CASH">Espèces</option>
                <option value="TRANSFER">Virement</option>
                <option value="CARD">Carte bancaire</option>
                <option value="MOBILE">Mobile Money</option>
              </select>
            </div>
          </div>
        </div>

        {/* Seller Info */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Vendeur</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                IFU / Identifiant fiscal *
              </label>
              <input
                type="text"
                placeholder="CI1234567"
                value={formData.seller.taxId}
                onChange={(e) => setFormData({ ...formData, seller: { ...formData.seller, taxId: e.target.value }})}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors['seller.taxId'] ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors['seller.taxId'] && <p className="text-red-500 text-xs mt-1">{errors['seller.taxId']}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nom de l'entreprise *
              </label>
              <input
                type="text"
                placeholder="Oxalio SARL"
                value={formData.seller.companyName}
                onChange={(e) => setFormData({ ...formData, seller: { ...formData.seller, companyName: e.target.value }})}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors['seller.companyName'] ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors['seller.companyName'] && <p className="text-red-500 text-xs mt-1">{errors['seller.companyName']}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Adresse *
              </label>
              <input
                type="text"
                placeholder="Abidjan, Plateau"
                value={formData.seller.address}
                onChange={(e) => setFormData({ ...formData, seller: { ...formData.seller, address: e.target.value }})}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors['seller.address'] ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors['seller.address'] && <p className="text-red-500 text-xs mt-1">{errors['seller.address']}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                type="email"
                placeholder="contact@oxalio.com"
                value={formData.seller.email}
                onChange={(e) => setFormData({ ...formData, seller: { ...formData.seller, email: e.target.value }})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Téléphone
              </label>
              <input
                type="tel"
                placeholder="+2250701020304"
                value={formData.seller.phone}
                onChange={(e) => setFormData({ ...formData, seller: { ...formData.seller, phone: e.target.value }})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
        </div>

        {/* Buyer Info */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Acheteur</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                IFU / Identifiant fiscal *
              </label>
              <input
                type="text"
                placeholder="CI7654321"
                value={formData.buyer.taxId}
                onChange={(e) => setFormData({ ...formData, buyer: { ...formData.buyer, taxId: e.target.value }})}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors['buyer.taxId'] ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors['buyer.taxId'] && <p className="text-red-500 text-xs mt-1">{errors['buyer.taxId']}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nom du client *
              </label>
              <input
                type="text"
                placeholder="Client Démo"
                value={formData.buyer.name}
                onChange={(e) => setFormData({ ...formData, buyer: { ...formData.buyer, name: e.target.value }})}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors['buyer.name'] ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors['buyer.name'] && <p className="text-red-500 text-xs mt-1">{errors['buyer.name']}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Adresse *
              </label>
              <input
                type="text"
                placeholder="Cocody, Riviera"
                value={formData.buyer.address}
                onChange={(e) => setFormData({ ...formData, buyer: { ...formData.buyer, address: e.target.value }})}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors['buyer.address'] ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {errors['buyer.address'] && <p className="text-red-500 text-xs mt-1">{errors['buyer.address']}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                type="email"
                placeholder="client@demo.com"
                value={formData.buyer.email}
                onChange={(e) => setFormData({ ...formData, buyer: { ...formData.buyer, email: e.target.value }})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Téléphone
              </label>
              <input
                type="tel"
                placeholder="+2250705060708"
                value={formData.buyer.phone}
                onChange={(e) => setFormData({ ...formData, buyer: { ...formData.buyer, phone: e.target.value }})}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
        </div>

        {/* Invoice Lines */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-900">Lignes de facture</h2>
            <button
              type="button"
              onClick={handleAddLine}
              className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus size={18} />
              Ajouter une ligne
            </button>
          </div>

          <div className="space-y-4">
            {formData.lines.map((line, index) => (
              <div key={index} className="border border-gray-200 p-4 rounded-lg">
                <div className="flex justify-between items-center mb-3">
                  <span className="font-medium text-gray-700">Ligne {index + 1}</span>
                  {formData.lines.length > 1 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveLine(index)}
                      className="text-red-600 hover:text-red-800"
                    >
                      <Trash2 size={18} />
                    </button>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-6 gap-3">
                  <div className="md:col-span-2">
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Description *
                    </label>
                    <input
                      type="text"
                      placeholder="Produit A"
                      value={line.description}
                      onChange={(e) => handleLineChange(index, 'description', e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                        errors[`lines[${index}].description`] ? 'border-red-500' : 'border-gray-300'
                      }`}
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Quantité *
                    </label>
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={line.quantity}
                      onChange={(e) => handleLineChange(index, 'quantity', parseFloat(e.target.value) || 0)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Prix unitaire *
                    </label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={line.unitPrice}
                      onChange={(e) => handleLineChange(index, 'unitPrice', parseFloat(e.target.value) || 0)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      TVA %
                    </label>
                    <input
                      type="number"
                      min="0"
                      max="100"
                      step="0.01"
                      value={line.vatRate}
                      onChange={(e) => handleLineChange(index, 'vatRate', parseFloat(e.target.value) || 0)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Remise
                    </label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={line.discount}
                      onChange={(e) => handleLineChange(index, 'discount', parseFloat(e.target.value) || 0)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div className="mt-2">
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    Code produit
                  </label>
                  <input
                    type="text"
                    placeholder="PROD-A001"
                    value={line.productCode}
                    onChange={(e) => handleLineChange(index, 'productCode', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div className="mt-3 pt-3 border-t border-gray-200 text-right">
                  <span className="text-sm text-gray-600">Total ligne: </span>
                  <span className="text-lg font-bold text-gray-900">
                    {formatAmount((line.quantity * line.unitPrice) - line.discount + line.vatAmount)} {formData.currency}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Totals Summary */}
        <div className="bg-gradient-to-r from-blue-50 to-blue-100 p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Récapitulatif</h2>
          <div className="space-y-2">
            <div className="flex justify-between text-gray-700">
              <span>Sous-total:</span>
              <span className="font-semibold">{formatAmount(formData.totals.subtotal)} {formData.currency}</span>
            </div>
            <div className="flex justify-between text-gray-700">
              <span>TVA totale:</span>
              <span className="font-semibold">{formatAmount(formData.totals.totalVat)} {formData.currency}</span>
            </div>
            {formData.totals.totalDiscount > 0 && (
              <div className="flex justify-between text-red-600">
                <span>Remise totale:</span>
                <span className="font-semibold">-{formatAmount(formData.totals.totalDiscount)} {formData.currency}</span>
              </div>
            )}
            <div className="border-t-2 border-blue-300 pt-2 mt-2">
              <div className="flex justify-between text-2xl font-bold text-gray-900">
                <span>TOTAL:</span>
                <span>{formatAmount(formData.totals.totalAmount)} {formData.currency}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Notes */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Notes (optionnel)
          </label>
          <textarea
            rows={3}
            placeholder="Notes additionnelles..."
            value={formData.notes}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        {/* Submit Button */}
        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/invoices')}
            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Annuler
          </button>
          <button
            type="submit"
            disabled={loading}
            className="flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                Création en cours...
              </>
            ) : (
              <>
                <Save size={20} />
                Créer la facture
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateInvoice;