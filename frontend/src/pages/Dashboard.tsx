import { useEffect, useState } from 'react';

interface Facture {
  id: number;
  reference: string;
  clientName: string;
  amount: number;
  taxAmount: number;
  date: string;
  status: 'certified' | 'paid' | 'cancelled';
  verificationUrl: string;
  description: string;
}

// Factures initiales (celles déjà créées)
const FACTURES_INITIALES: Facture[] = [
  {
    id: 1,
    reference: "2505842N26000000098",
    clientName: "Jean KOUASSI",
    amount: 59000,
    taxAmount: 9000,
    date: "2026-01-21",
    status: "certified",
    verificationUrl: "http://54.247.95.108/fr/verification/019be102-5ccd-7cc0-8be5-c379a00d1860",
    description: "Consultation informatique"
  },
  {
    id: 2,
    reference: "2505842N26000000101",
    clientName: "Marie DIALLO",
    amount: 177000,
    taxAmount: 27000,
    date: "2026-01-21",
    status: "certified",
    verificationUrl: "http://54.247.95.108/fr/verification/019be123-3c6e-7226-ba5d-e314e2fed7ad",
    description: "Formation React + TypeScript"
  },
  {
    id: 3,
    reference: "2505842N26000000102",
    clientName: "Kouadio KONAN",
    amount: 29500,
    taxAmount: 4500,
    date: "2026-01-21",
    status: "certified",
    verificationUrl: "http://54.247.95.108/fr/verification/019be131-4106-7226-ba5e-26433899eb74",
    description: "Maintenance ordinateur"
  }
];

export default function Dashboard() {
  const [factures, setFactures] = useState<Facture[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [newFacture, setNewFacture] = useState({
    reference: '',
    clientName: '',
    amount: '',
    taxAmount: '',
    description: '',
    verificationUrl: ''
  });

  // Charger les factures au démarrage
  useEffect(() => {
    const saved = localStorage.getItem('oxalio-factures');
    if (saved) {
      setFactures(JSON.parse(saved));
    } else {
      setFactures(FACTURES_INITIALES);
      localStorage.setItem('oxalio-factures', JSON.stringify(FACTURES_INITIALES));
    }
  }, []);

  // Ajouter une nouvelle facture
  const addFacture = () => {
    const nouvelleFacture: Facture = {
      id: factures.length + 1,
      reference: newFacture.reference,
      clientName: newFacture.clientName,
      amount: parseInt(newFacture.amount),
      taxAmount: parseInt(newFacture.taxAmount),
      date: new Date().toISOString().split('T')[0],
      status: 'certified',
      verificationUrl: newFacture.verificationUrl,
      description: newFacture.description
    };

    const updated = [...factures, nouvelleFacture];
    setFactures(updated);
    localStorage.setItem('oxalio-factures', JSON.stringify(updated));
    
    // Reset form
    setNewFacture({
      reference: '',
      clientName: '',
      amount: '',
      taxAmount: '',
      description: '',
      verificationUrl: ''
    });
    setShowAddModal(false);
  };

  const stats = {
    totalFactures: factures.length,
    certifiees: factures.filter(f => f.status === 'certified').length,
    montantTotal: factures.reduce((sum, f) => sum + f.amount, 0),
    montantTVA: factures.reduce((sum, f) => sum + f.taxAmount, 0),
  };

  const getStatusColor = (status: string) => {
    switch(status) {
      case 'certified': return 'bg-green-100 text-green-800';
      case 'paid': return 'bg-blue-100 text-blue-800';
      case 'cancelled': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusLabel = (status: string) => {
    switch(status) {
      case 'certified': return 'Certifiée FNE';
      case 'paid': return 'Payée';
      case 'cancelled': return 'Annulée';
      default: return status;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* En-tête */}
        <div className="mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Dashboard OXALIO</h1>
            <p className="text-gray-600 mt-1">
              Système de facturation électronique FNE
            </p>
          </div>
          <button
            onClick={() => setShowAddModal(true)}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition flex items-center gap-2"
          >
            <span className="text-xl">+</span>
            Ajouter une facture
          </button>
        </div>

        {/* Modal d'ajout */}
        {showAddModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
              <h3 className="text-xl font-bold mb-4">Ajouter une facture FNE</h3>
              
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Référence FNE</label>
                  <input
                    type="text"
                    placeholder="2505842N26000000102"
                    value={newFacture.reference}
                    onChange={(e) => setNewFacture({...newFacture, reference: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Client</label>
                  <input
                    type="text"
                    placeholder="Nom du client"
                    value={newFacture.clientName}
                    onChange={(e) => setNewFacture({...newFacture, clientName: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Montant TTC</label>
                    <input
                      type="number"
                      placeholder="29500"
                      value={newFacture.amount}
                      onChange={(e) => setNewFacture({...newFacture, amount: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">TVA</label>
                    <input
                      type="number"
                      placeholder="4500"
                      value={newFacture.taxAmount}
                      onChange={(e) => setNewFacture({...newFacture, taxAmount: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                  <input
                    type="text"
                    placeholder="Maintenance ordinateur"
                    value={newFacture.description}
                    onChange={(e) => setNewFacture({...newFacture, description: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Lien de vérification DGI</label>
                  <input
                    type="text"
                    placeholder="http://54.247.95.108/fr/verification/..."
                    value={newFacture.verificationUrl}
                    onChange={(e) => setNewFacture({...newFacture, verificationUrl: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
              </div>

              <div className="flex gap-3 mt-6">
                <button
                  onClick={() => setShowAddModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Annuler
                </button>
                <button
                  onClick={addFacture}
                  className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
                >
                  Ajouter
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Cartes de statistiques */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          
          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Total Factures</p>
                <p className="text-3xl font-bold text-gray-900">{stats.totalFactures}</p>
              </div>
              <div className="text-blue-500 text-4xl">📄</div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Certifiées FNE</p>
                <p className="text-3xl font-bold text-green-600">{stats.certifiees}</p>
              </div>
              <div className="text-green-500 text-4xl">✅</div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Montant Total</p>
                <p className="text-2xl font-bold text-gray-900">
                  {stats.montantTotal.toLocaleString('fr-FR')}
                </p>
                <p className="text-xs text-gray-500">FCFA</p>
              </div>
              <div className="text-purple-500 text-4xl">💰</div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-orange-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">TVA Collectée</p>
                <p className="text-2xl font-bold text-gray-900">
                  {stats.montantTVA.toLocaleString('fr-FR')}
                </p>
                <p className="text-xs text-gray-500">FCFA</p>
              </div>
              <div className="text-orange-500 text-4xl">📊</div>
            </div>
          </div>
        </div>

        {/* Bannière de succès */}
        <div className="bg-green-50 border border-green-200 rounded-lg p-6 mb-8">
          <div className="flex items-start">
            <div className="text-green-500 text-3xl mr-4">🎉</div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-green-900 mb-2">
                Système FNE opérationnel - {stats.certifiees} factures certifiées !
              </h3>
              <p className="text-green-700 mb-3">
                OXALIO SARL (NCC: 2505842N) - Total : <strong>{stats.montantTotal.toLocaleString('fr-FR')} FCFA</strong>
              </p>
            </div>
          </div>
        </div>

        {/* Liste des factures */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">
              Factures Certifiées FNE ({factures.length})
            </h2>
          </div>

          <div className="divide-y divide-gray-200">
            {factures.map((facture) => (
              <div key={facture.id} className="p-6 hover:bg-gray-50 transition">
                <div className="flex items-start justify-between">
                  
                  {/* Informations principales */}
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">
                        {facture.reference}
                      </h3>
                      <span className={`inline-flex px-3 py-1 text-xs font-medium rounded-full ${getStatusColor(facture.status)}`}>
                        {getStatusLabel(facture.status)}
                      </span>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-3">
                      <div>
                        <p className="text-sm text-gray-600">Client</p>
                        <p className="font-medium text-gray-900">{facture.clientName}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Description</p>
                        <p className="font-medium text-gray-900">{facture.description}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Date</p>
                        <p className="font-medium text-gray-900">
                          {new Date(facture.date).toLocaleDateString('fr-FR')}
                        </p>
                      </div>
                    </div>

                    <a 
                      href={facture.verificationUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-blue-800 hover:underline"
                    >
                      🔗 Vérifier sur le site de la DGI
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                      </svg>
                    </a>
                  </div>

                  {/* Montants */}
                  <div className="text-right ml-6">
                    <p className="text-sm text-gray-600 mb-1">Montant HT</p>
                    <p className="text-lg font-semibold text-gray-900">
                      {(facture.amount - facture.taxAmount).toLocaleString('fr-FR')} FCFA
                    </p>
                    <p className="text-sm text-gray-600 mt-2">TVA (18%)</p>
                    <p className="text-md font-medium text-gray-700">
                      {facture.taxAmount.toLocaleString('fr-FR')} FCFA
                    </p>
                    <div className="mt-3 pt-3 border-t border-gray-200">
                      <p className="text-sm text-gray-600">Total TTC</p>
                      <p className="text-2xl font-bold text-green-600">
                        {facture.amount.toLocaleString('fr-FR')} FCFA
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
