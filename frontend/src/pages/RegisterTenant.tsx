import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerTenant } from '../api/tenantService';
import type { CreateTenantRequest } from '../types/tenant-types';
import {
  SUBSCRIPTION_PLAN_LABELS,
  SUBSCRIPTION_PLAN_PRICES,
  SUBSCRIPTION_PLAN_LIMITS,
} from '../types/tenant-types';

export default function RegisterTenant() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [step, setStep] = useState(1); // 1: Info entreprise, 2: Contact, 3: Plan

  const [formData, setFormData] = useState<CreateTenantRequest>({
    companyName: '',
    ncc: '',
    ownerEmail: '',
    ownerName: '',
    ownerPhone: '',
    password: '',
    subscriptionPlan: 'starter',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    setLoading(true);
    setError(null);

    try {
      await registerTenant(formData);
      
      // Rediriger vers la page de confirmation
      navigate('/register/success', {
        state: { email: formData.ownerEmail },
      });
    } catch (err: any) {
      console.error('Erreur inscription:', err);
      setError(
        err.response?.data?.message ||
        'Une erreur est survenue lors de l\'inscription'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: keyof CreateTenantRequest, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl w-full">
        
        {/* En-tête */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">
            Rejoignez OXALIO
          </h1>
          <p className="text-lg text-gray-600">
            La plateforme de facturation électronique FNE pour les entreprises ivoiriennes
          </p>
        </div>

        {/* Indicateur d'étapes */}
        <div className="mb-8">
          <div className="flex items-center justify-center">
            {[1, 2, 3].map((s) => (
              <div key={s} className="flex items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold ${
                    step >= s
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-300 text-gray-600'
                  }`}
                >
                  {s}
                </div>
                {s < 3 && (
                  <div
                    className={`w-24 h-1 ${
                      step > s ? 'bg-blue-600' : 'bg-gray-300'
                    }`}
                  />
                )}
              </div>
            ))}
          </div>
          <div className="flex justify-between mt-2 text-sm text-gray-600">
            <span>Entreprise</span>
            <span>Contact</span>
            <span>Plan</span>
          </div>
        </div>

        {/* Formulaire */}
        <div className="bg-white rounded-lg shadow-xl p-8">
          
          {error && (
            <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            
            {/* Étape 1 : Informations entreprise */}
            {step === 1 && (
              <div className="space-y-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-4">
                  Informations de l'entreprise
                </h2>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nom de l'entreprise *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.companyName}
                    onChange={(e) => handleChange('companyName', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Ex: Restaurant Chez Koffi"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    NCC (Numéro de Compte Contribuable) *
                  </label>
                  <input
                    type="text"
                    required
                    pattern="[0-9]{7}[A-Z]"
                    value={formData.ncc}
                    onChange={(e) => handleChange('ncc', e.target.value.toUpperCase())}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Ex: 2505842N"
                    maxLength={8}
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    Format: 7 chiffres + 1 lettre (ex: 2505842N)
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => setStep(2)}
                  disabled={!formData.companyName || !formData.ncc}
                  className="w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                >
                  Suivant
                </button>
              </div>
            )}

            {/* Étape 2 : Contact */}
            {step === 2 && (
              <div className="space-y-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-4">
                  Informations de contact
                </h2>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nom complet *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.ownerName}
                    onChange={(e) => handleChange('ownerName', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Ex: Koffi KOUASSI"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email *
                  </label>
                  <input
                    type="email"
                    required
                    value={formData.ownerEmail}
                    onChange={(e) => handleChange('ownerEmail', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="contact@exemple.ci"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Téléphone
                  </label>
                  <input
                    type="tel"
                    pattern="0[0-9]{9}"
                    value={formData.ownerPhone}
                    onChange={(e) => handleChange('ownerPhone', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="0700000000"
                    maxLength={10}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Mot de passe *
                  </label>
                  <input
                    type="password"
                    required
                    minLength={8}
                    value={formData.password}
                    onChange={(e) => handleChange('password', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Minimum 8 caractères"
                  />
                </div>

                <div className="flex gap-4">
                  <button
                    type="button"
                    onClick={() => setStep(1)}
                    className="flex-1 py-3 border border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition"
                  >
                    Retour
                  </button>
                  <button
                    type="button"
                    onClick={() => setStep(3)}
                    disabled={!formData.ownerName || !formData.ownerEmail || !formData.password}
                    className="flex-1 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                  >
                    Suivant
                  </button>
                </div>
              </div>
            )}

            {/* Étape 3 : Choix du plan */}
            {step === 3 && (
              <div className="space-y-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-4">
                  Choisissez votre plan
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {(['starter', 'professional', 'enterprise'] as const).map((plan) => (
                    <div
                      key={plan}
                      onClick={() => handleChange('subscriptionPlan', plan)}
                      className={`cursor-pointer border-2 rounded-lg p-6 transition ${
                        formData.subscriptionPlan === plan
                          ? 'border-blue-600 bg-blue-50'
                          : 'border-gray-200 hover:border-blue-300'
                      }`}
                    >
                      <div className="text-center">
                        <h3 className="text-xl font-bold text-gray-900 mb-2">
                          {SUBSCRIPTION_PLAN_LABELS[plan]}
                        </h3>
                        <div className="text-3xl font-bold text-blue-600 mb-1">
                          {SUBSCRIPTION_PLAN_PRICES[plan].toLocaleString()} FCFA
                        </div>
                        <div className="text-sm text-gray-600 mb-4">par mois</div>
                        <div className="text-sm text-gray-700 mb-4">
                          {SUBSCRIPTION_PLAN_LIMITS[plan] === 999999
                            ? 'Factures illimitées'
                            : `${SUBSCRIPTION_PLAN_LIMITS[plan]} factures/mois`}
                        </div>
                        {plan === 'starter' && (
                          <div className="text-xs text-green-600 font-semibold">
                            30 jours d'essai gratuit
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>

                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <p className="text-green-800 text-sm">
                    ✅ <strong>30 jours d'essai gratuit</strong> - Aucun paiement requis maintenant
                  </p>
                </div>

                <div className="flex gap-4">
                  <button
                    type="button"
                    onClick={() => setStep(2)}
                    className="flex-1 py-3 border border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition"
                  >
                    Retour
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 py-3 bg-green-600 text-white rounded-lg font-semibold hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                  >
                    {loading ? 'Inscription...' : 'Créer mon compte'}
                  </button>
                </div>
              </div>
            )}
          </form>
        </div>

        {/* Pied de page */}
        <div className="mt-8 text-center text-sm text-gray-600">
          <p>
            Vous avez déjà un compte ?{' '}
            <a href="/login" className="text-blue-600 hover:underline font-semibold">
              Se connecter
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}