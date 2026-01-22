import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getTenantById,
  updateTenant,
  upgradeTenantPlan,
  deleteTenant,
} from '../api/tenantService';
import type { Tenant, UpdateTenantRequest } from '../types/tenant-types';
import {
  SUBSCRIPTION_PLAN_LABELS,
  SUBSCRIPTION_STATUS_LABELS,
  SUBSCRIPTION_STATUS_COLORS,
  SUBSCRIPTION_PLAN_PRICES,
} from '../types/tenant-types';
import { calculateTrialDaysLeft, calculateInvoiceUsagePercent } from '../api/tenantService';

export default function TenantDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [tenant, setTenant] = useState<Tenant | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState<UpdateTenantRequest>({});

  useEffect(() => {
    if (id) {
      loadTenant();
    }
  }, [id]);

  const loadTenant = async () => {
    try {
      setLoading(true);
      const data = await getTenantById(Number(id));
      setTenant(data);
      setFormData({
        companyName: data.companyName,
        ownerEmail: data.ownerEmail,
        ownerName: data.ownerName,
        ownerPhone: data.ownerPhone || '',
        fneApiKey: data.fneApiKey || '',
      });
    } catch (error) {
      console.error('Erreur chargement tenant:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    if (!tenant) return;

    try {
      await updateTenant(tenant.id, formData);
      await loadTenant();
      setEditing(false);
    } catch (error) {
      console.error('Erreur mise à jour:', error);
    }
  };

  const handleUpgrade = async (newPlan: string) => {
    if (!tenant) return;

    if (window.confirm(`Confirmer l'upgrade vers le plan ${newPlan} ?`)) {
      try {
        await upgradeTenantPlan(tenant.id, newPlan);
        await loadTenant();
      } catch (error) {
        console.error('Erreur upgrade:', error);
      }
    }
  };

  const handleDelete = async () => {
    if (!tenant) return;

    if (window.confirm(`⚠️ ATTENTION: Supprimer définitivement le tenant "${tenant.companyName}" ?\n\nToutes les données seront perdues !`)) {
      try {
        await deleteTenant(tenant.id);
        navigate('/admin/tenants');
      } catch (error) {
        console.error('Erreur suppression:', error);
      }
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!tenant) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">Tenant introuvable</p>
          <button
            onClick={() => navigate('/admin/tenants')}
            className="mt-4 text-blue-600 hover:underline"
          >
            Retour à la liste
          </button>
        </div>
      </div>
    );
  }

  const usagePercent = calculateInvoiceUsagePercent(tenant);
  const trialDaysLeft = calculateTrialDaysLeft(tenant);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* En-tête */}
        <div className="mb-6 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/admin/tenants')}
              className="text-gray-600 hover:text-gray-900"
            >
              ← Retour
            </button>
            <h1 className="text-3xl font-bold text-gray-900">
              {tenant.companyName}
            </h1>
            <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${
              SUBSCRIPTION_STATUS_COLORS[tenant.subscriptionStatus]
            }`}>
              {SUBSCRIPTION_STATUS_LABELS[tenant.subscriptionStatus]}
            </span>
          </div>

          <button
            onClick={() => setEditing(!editing)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            {editing ? 'Annuler' : 'Modifier'}
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Colonne gauche - Informations principales */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Informations générales */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Informations générales
              </h2>

              {editing ? (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nom de l'entreprise
                    </label>
                    <input
                      type="text"
                      value={formData.companyName || ''}
                      onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      value={formData.ownerEmail || ''}
                      onChange={(e) => setFormData({ ...formData, ownerEmail: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nom du propriétaire
                    </label>
                    <input
                      type="text"
                      value={formData.ownerName || ''}
                      onChange={(e) => setFormData({ ...formData, ownerName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Téléphone
                    </label>
                    <input
                      type="tel"
                      value={formData.ownerPhone || ''}
                      onChange={(e) => setFormData({ ...formData, ownerPhone: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Clé API FNE
                    </label>
                    <input
                      type="password"
                      value={formData.fneApiKey || ''}
                      onChange={(e) => setFormData({ ...formData, fneApiKey: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    />
                  </div>

                  <button
                    onClick={handleUpdate}
                    className="w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
                  >
                    Enregistrer les modifications
                  </button>
                </div>
              ) : (
                <dl className="space-y-3">
                  <div>
                    <dt className="text-sm font-medium text-gray-500">NCC</dt>
                    <dd className="text-lg font-mono text-gray-900">{tenant.ncc}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Slug</dt>
                    <dd className="text-lg text-gray-900">{tenant.slug}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Email</dt>
                    <dd className="text-lg text-gray-900">{tenant.ownerEmail}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Propriétaire</dt>
                    <dd className="text-lg text-gray-900">{tenant.ownerName}</dd>
                  </div>
                  {tenant.ownerPhone && (
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Téléphone</dt>
                      <dd className="text-lg text-gray-900">{tenant.ownerPhone}</dd>
                    </div>
                  )}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Inscrit le</dt>
                    <dd className="text-lg text-gray-900">
                      {new Date(tenant.createdAt).toLocaleDateString('fr-FR', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric',
                      })}
                    </dd>
                  </div>
                </dl>
              )}
            </div>

            {/* Utilisation des factures */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Utilisation des factures
              </h2>

              <div className="mb-4">
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-gray-600">Ce mois</span>
                  <span className="font-semibold">
                    {tenant.monthlyInvoiceCount} / {tenant.monthlyInvoiceLimit}
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-4">
                  <div
                    className={`h-4 rounded-full transition-all ${
                      usagePercent >= 90 ? 'bg-red-500' :
                      usagePercent >= 70 ? 'bg-orange-500' :
                      'bg-green-500'
                    }`}
                    style={{ width: `${Math.min(usagePercent, 100)}%` }}
                  />
                </div>
                <p className="text-sm text-gray-600 mt-2">
                  {usagePercent}% utilisé
                </p>
              </div>

              {usagePercent >= 90 && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                  <p className="text-red-800 text-sm">
                    ⚠️ Limite presque atteinte ! Suggérer un upgrade.
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Colonne droite - Abonnement et actions */}
          <div className="space-y-6">
            
            {/* Abonnement */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Abonnement
              </h2>

              <div className="text-center mb-4">
                <div className="text-3xl font-bold text-blue-600 mb-1">
                  {SUBSCRIPTION_PLAN_LABELS[tenant.subscriptionPlan]}
                </div>
                <div className="text-2xl font-semibold text-gray-900">
                  {SUBSCRIPTION_PLAN_PRICES[tenant.subscriptionPlan].toLocaleString()} FCFA
                </div>
                <div className="text-sm text-gray-600">par mois</div>
              </div>

              {tenant.subscriptionStatus === 'trial' && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
                  <p className="text-blue-800 text-sm text-center">
                    🎯 <strong>{trialDaysLeft} jours</strong> d'essai restants
                  </p>
                </div>
              )}

              <div className="space-y-2">
                <button
                  onClick={() => handleUpgrade('professional')}
                  disabled={tenant.subscriptionPlan === 'enterprise'}
                  className="w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  Upgrade vers Professional
                </button>
                <button
                  onClick={() => handleUpgrade('enterprise')}
                  disabled={tenant.subscriptionPlan === 'enterprise'}
                  className="w-full py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  Upgrade vers Enterprise
                </button>
              </div>

              <div className="mt-4 pt-4 border-t border-gray-200">
                <div className="text-sm text-gray-600">
                  <div className="flex justify-between mb-2">
                    <span>Début:</span>
                    <span className="font-semibold">
                      {new Date(tenant.subscriptionStartedAt).toLocaleDateString('fr-FR')}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span>Fin:</span>
                    <span className="font-semibold">
                      {new Date(tenant.subscriptionEndsAt).toLocaleDateString('fr-FR')}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Actions dangereuses */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Zone de danger
              </h2>

              <button
                onClick={handleDelete}
                className="w-full py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
              >
                Supprimer le tenant
              </button>

              <p className="text-xs text-gray-500 mt-2 text-center">
                Cette action est irréversible
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}