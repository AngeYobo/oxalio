// src/pages/Register.tsx
import { useState } from 'react';
import { apiClient } from '../api/client';
import { useNavigate } from 'react-router-dom';

interface RegisterForm {
  // Entreprise
  companyName: string;
  ncc: string;
  
  // FNE
  fneApiKey: string;
  fneEstablishment: string;
  fnePointOfSale: string;
  
  // Contact
  email: string;
  phone: string;
  address: string;
  
  // Compte admin
  adminFirstName: string;
  adminLastName: string;
  password: string;
  confirmPassword: string;
  
  // Abonnement
  plan: 'starter' | 'pro' | 'enterprise';
}

export default function Register() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [form, setForm] = useState<RegisterForm>({
    companyName: '',
    ncc: '',
    fneApiKey: '',
    fneEstablishment: '',
    fnePointOfSale: '',
    email: '',
    phone: '',
    address: '',
    adminFirstName: '',
    adminLastName: '',
    password: '',
    confirmPassword: '',
    plan: 'starter'
  });

  const handleSubmit = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await apiClient.post('/auth/register-tenant', form);
      
      // Rediriger vers login ou dashboard
      navigate('/login', { 
        state: { message: 'Inscription réussie ! Connectez-vous.' }
      });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erreur lors de l\'inscription');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
      <div className="max-w-4xl w-full">
        
        {/* En-tête */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Rejoignez OXALIO</h1>
          <p className="text-gray-600 mt-2">
            La solution de facturation FNE pour les entreprises ivoiriennes
          </p>
        </div>

        {/* Progression */}
        <div className="flex justify-between mb-8">
          {[1, 2, 3, 4].map((s) => (
            <div key={s} className="flex items-center flex-1">
              <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                step >= s ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-600'
              }`}>
                {s}
              </div>
              {s < 4 && <div className={`flex-1 h-1 ${step > s ? 'bg-blue-600' : 'bg-gray-200'}`} />}
            </div>
          ))}
        </div>

        {/* Formulaire */}
        <div className="bg-white rounded-lg shadow p-8">
          
          {/* Étape 1 : Entreprise */}
          {step === 1 && (
            <div className="space-y-6">
              <h2 className="text-xl font-bold">Informations de votre entreprise</h2>
              
              <div>
                <label className="block text-sm font-medium mb-2">Nom de l'entreprise *</label>
                <input
                  type="text"
                  value={form.companyName}
                  onChange={(e) => setForm({...form, companyName: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  placeholder="Ex: Restaurant Chez Koffi"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">NCC (Numéro de Compte Contribuable) *</label>
                <input
                  type="text"
                  value={form.ncc}
                  onChange={(e) => setForm({...form, ncc: e.target.value.toUpperCase()})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  placeholder="Ex: 1234567A"
                  maxLength={8}
                />
                <p className="text-xs text-gray-500 mt-1">Format: 7 chiffres + 1 lettre</p>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">Email *</label>
                  <input
                    type="email"
                    value={form.email}
                    onChange={(e) => setForm({...form, email: e.target.value})}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2">Téléphone *</label>
                  <input
                    type="tel"
                    value={form.phone}
                    onChange={(e) => setForm({...form, phone: e.target.value})}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    placeholder="0700000000"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Adresse</label>
                <textarea
                  value={form.address}
                  onChange={(e) => setForm({...form, address: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  rows={3}
                />
              </div>
            </div>
          )}

          {/* Étape 2 : Configuration FNE */}
          {step === 2 && (
            <div className="space-y-6">
              <h2 className="text-xl font-bold">Configuration FNE</h2>
              
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="text-sm text-blue-800">
                  <strong>Important :</strong> Vous devez avoir vos credentials FNE de la DGI.
                  Si vous ne les avez pas encore, vous pouvez les obtenir sur{' '}
                  <a href="https://developper.e-impots.gouv.ci" target="_blank" className="underline">
                    developper.e-impots.gouv.ci
                  </a>
                </p>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Clé API FNE *</label>
                <input
                  type="password"
                  value={form.fneApiKey}
                  onChange={(e) => setForm({...form, fneApiKey: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  placeholder="Votre clé API FNE"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Établissement</label>
                <input
                  type="text"
                  value={form.fneEstablishment}
                  onChange={(e) => setForm({...form, fneEstablishment: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  placeholder="Ex: Agence Abidjan"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Point de vente</label>
                <input
                  type="text"
                  value={form.fnePointOfSale}
                  onChange={(e) => setForm({...form, fnePointOfSale: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  placeholder="Ex: Caisse 1"
                />
              </div>
            </div>
          )}

          {/* Étape 3 : Compte administrateur */}
          {step === 3 && (
            <div className="space-y-6">
              <h2 className="text-xl font-bold">Créez votre compte administrateur</h2>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">Prénom *</label>
                  <input
                    type="text"
                    value={form.adminFirstName}
                    onChange={(e) => setForm({...form, adminFirstName: e.target.value})}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2">Nom *</label>
                  <input
                    type="text"
                    value={form.adminLastName}
                    onChange={(e) => setForm({...form, adminLastName: e.target.value})}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Mot de passe *</label>
                <input
                  type="password"
                  value={form.password}
                  onChange={(e) => setForm({...form, password: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
                <p className="text-xs text-gray-500 mt-1">Minimum 8 caractères</p>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Confirmer le mot de passe *</label>
                <input
                  type="password"
                  value={form.confirmPassword}
                  onChange={(e) => setForm({...form, confirmPassword: e.target.value})}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
              </div>
            </div>
          )}

          {/* Étape 4 : Choix du plan */}
          {step === 4 && (
            <div className="space-y-6">
              <h2 className="text-xl font-bold">Choisissez votre abonnement</h2>
              
              <div className="grid grid-cols-3 gap-4">
                {/* Plan Starter */}
                <div 
                  onClick={() => setForm({...form, plan: 'starter'})}
                  className={`border-2 rounded-lg p-6 cursor-pointer transition ${
                    form.plan === 'starter' ? 'border-blue-600 bg-blue-50' : 'border-gray-200'
                  }`}
                >
                  <h3 className="text-lg font-bold mb-2">Starter</h3>
                  <p className="text-3xl font-bold mb-4">25,000 <span className="text-sm">FCFA/mois</span></p>
                  <ul className="space-y-2 text-sm">
                    <li>✓ 50 factures/mois</li>
                    <li>✓ 1 utilisateur</li>
                    <li>✓ Support email</li>
                  </ul>
                </div>

                {/* Plan Pro */}
                <div 
                  onClick={() => setForm({...form, plan: 'pro'})}
                  className={`border-2 rounded-lg p-6 cursor-pointer transition ${
                    form.plan === 'pro' ? 'border-blue-600 bg-blue-50' : 'border-gray-200'
                  }`}
                >
                  <div className="bg-blue-600 text-white text-xs px-2 py-1 rounded mb-2 inline-block">
                    POPULAIRE
                  </div>
                  <h3 className="text-lg font-bold mb-2">Professional</h3>
                  <p className="text-3xl font-bold mb-4">50,000 <span className="text-sm">FCFA/mois</span></p>
                  <ul className="space-y-2 text-sm">
                    <li>✓ 500 factures/mois</li>
                    <li>✓ 5 utilisateurs</li>
                    <li>✓ API access</li>
                    <li>✓ Support prioritaire</li>
                  </ul>
                </div>

                {/* Plan Enterprise */}
                <div 
                  onClick={() => setForm({...form, plan: 'enterprise'})}
                  className={`border-2 rounded-lg p-6 cursor-pointer transition ${
                    form.plan === 'enterprise' ? 'border-blue-600 bg-blue-50' : 'border-gray-200'
                  }`}
                >
                  <h3 className="text-lg font-bold mb-2">Enterprise</h3>
                  <p className="text-3xl font-bold mb-4">150,000 <span className="text-sm">FCFA/mois</span></p>
                  <ul className="space-y-2 text-sm">
                    <li>✓ Factures illimitées</li>
                    <li>✓ Utilisateurs illimités</li>
                    <li>✓ API illimitée</li>
                    <li>✓ Support dédié</li>
                    <li>✓ Personnalisation</li>
                  </ul>
                </div>
              </div>

              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <p className="text-sm text-green-800">
                  <strong>14 jours d'essai gratuit</strong> - Aucune carte bancaire requise
                </p>
              </div>
            </div>
          )}

          {/* Message d'erreur */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          {/* Navigation */}
          <div className="flex justify-between mt-8">
            {step > 1 && (
              <button
                onClick={() => setStep(step - 1)}
                className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Précédent
              </button>
            )}
            
            {step < 4 ? (
              <button
                onClick={() => setStep(step + 1)}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 ml-auto"
              >
                Suivant
              </button>
            ) : (
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 ml-auto disabled:opacity-50"
              >
                {loading ? 'Inscription...' : 'Créer mon compte'}
              </button>
            )}
          </div>
        </div>

        {/* Lien de connexion */}
        <p className="text-center mt-6 text-sm text-gray-600">
          Vous avez déjà un compte ?{' '}
          <a href="/login" className="text-blue-600 hover:underline">
            Connectez-vous
          </a>
        </p>
      </div>
    </div>
  );
}