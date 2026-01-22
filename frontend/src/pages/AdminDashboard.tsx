import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  getAllTenants,
  getTenantStats,
  toggleTenantStatus,
} from '../api/tenantService';
import type { Tenant, TenantStats } from '../types/tenant-types';
import {
  SUBSCRIPTION_PLAN_LABELS,
  SUBSCRIPTION_STATUS_LABELS,
  SUBSCRIPTION_STATUS_COLORS,
  SUBSCRIPTION_PLAN_PRICES,
} from '../types/tenant-types';
import { calculateTrialDaysLeft, calculateInvoiceUsagePercent } from '../api/tenantService';

export default function AdminDashboard() {
  // ========================================
  // STATES - Tous déclarés ici
  // ========================================
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [stats, setStats] = useState<TenantStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);  // ✅ IMPORTANT
  const [filter, setFilter] = useState<'all' | 'active' | 'trial'>('all');
  const [searchQuery, setSearchQuery] = useState('');

  // ========================================
  // EFFECTS
  // ========================================
  useEffect(() => {
    console.log('🔄 AdminDashboard mounted');
    loadData();
  }, []);

  // ========================================
  // FUNCTIONS
  // ========================================
  const loadData = async () => {
    console.log('📡 Loading data...');
    try {
      setLoading(true);
      setError(null);
      
      const [tenantsData, statsData] = await Promise.all([
        getAllTenants(),
        getTenantStats(),
      ]);
      
      console.log('✅ Data loaded:', { 
        tenants: tenantsData.length, 
        stats: statsData 
      });
      
      setTenants(tenantsData);
      setStats(statsData);
    } catch (error: any) {
      console.error('❌ Error:', error);
      setError(error.message || 'Erreur de chargement');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (tenantId: number, currentStatus: boolean) => {
    try {
      await toggleTenantStatus(tenantId, !currentStatus);
      await loadData();
    } catch (error) {
      console.error('Erreur changement statut:', error);
    }
  };

  // ========================================
  // COMPUTED VALUES
  // ========================================
  const filteredTenants = tenants.filter((tenant) => {
    if (filter === 'active' && !tenant.isActive) return false;
    if (filter === 'trial' && tenant.subscriptionStatus !== 'trial') return false;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      return (
        tenant.companyName.toLowerCase().includes(query) ||
        tenant.ncc.toLowerCase().includes(query) ||
        tenant.ownerEmail.toLowerCase().includes(query)
      );
    }

    return true;
  });

  // ========================================
  // RENDER
  // ========================================
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Chargement...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md">
          <h3 className="text-lg font-semibold text-red-900 mb-2">
            Erreur de chargement
          </h3>
          <p className="text-red-700 mb-4">{error}</p>
          <button
            onClick={loadData}
            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
          >
            Réessayer
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* En-tête */}
        <div className="mb-8">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Dashboard Admin OXALIO</h1>
              <p className="text-gray-600 mt-1">
                Gestion de la plateforme SaaS multi-tenant
              </p>
            </div>
            <button
              onClick={loadData}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Actualiser
            </button>
          </div>
        </div>

        {/* Statistiques globales */}
        {stats && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            
            <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">Total Clients</p>
                  <p className="text-3xl font-bold text-gray-900">{stats.totalTenants}</p>
                  <p className="text-sm text-green-600 mt-1">
                    {stats.activeTenants} actifs
                  </p>
                </div>
                <div className="text-blue-500 text-4xl">👥</div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">En essai</p>
                  <p className="text-3xl font-bold text-gray-900">{stats.trialTenants}</p>
                  <p className="text-sm text-gray-500 mt-1">
                    30 jours gratuits
                  </p>
                </div>
                <div className="text-green-500 text-4xl">🎯</div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">Revenus mensuels</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {((stats.starterPlanCount || 0) * 25000 + 
                      (stats.professionalPlanCount || 0) * 50000 + 
                      (stats.enterprisePlanCount || 0) * 150000).toLocaleString('fr-FR')}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">FCFA / mois</p>
                </div>
                <div className="text-purple-500 text-4xl">💰</div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6 border-l-4 border-orange-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">Factures ce mois</p>
                  <p className="text-3xl font-bold text-gray-900">
                    {stats.totalInvoicesThisMonth || 0}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">
                    Toutes plateformes
                  </p>
                </div>
                <div className="text-orange-500 text-4xl">📄</div>
              </div>
            </div>
          </div>
        )}

        {/* Répartition par plan */}
        {stats && (
          <div className="bg-white rounded-lg shadow p-6 mb-8">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Répartition par plan d'abonnement
            </h2>
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center p-4 bg-blue-50 rounded-lg">
                <div className="text-2xl font-bold text-blue-600">
                  {stats.starterPlanCount}
                </div>
                <div className="text-sm text-gray-600 mt-1">Starter</div>
                <div className="text-xs text-gray-500 mt-1">
                  {(stats.starterPlanCount * 25000).toLocaleString()} FCFA/mois
                </div>
              </div>
              <div className="text-center p-4 bg-green-50 rounded-lg">
                <div className="text-2xl font-bold text-green-600">
                  {stats.professionalPlanCount}
                </div>
                <div className="text-sm text-gray-600 mt-1">Professional</div>
                <div className="text-xs text-gray-500 mt-1">
                  {(stats.professionalPlanCount * 50000).toLocaleString()} FCFA/mois
                </div>
              </div>
              <div className="text-center p-4 bg-purple-50 rounded-lg">
                <div className="text-2xl font-bold text-purple-600">
                  {stats.enterprisePlanCount}
                </div>
                <div className="text-sm text-gray-600 mt-1">Enterprise</div>
                <div className="text-xs text-gray-500 mt-1">
                  {(stats.enterprisePlanCount * 150000).toLocaleString()} FCFA/mois
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Filtres et recherche */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="flex flex-col md:flex-row gap-4">
            
            {/* Recherche */}
            <div className="flex-1">
              <input
                type="text"
                placeholder="Rechercher par nom, NCC ou email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* Filtres */}
            <div className="flex gap-2">
              <button
                onClick={() => setFilter('all')}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  filter === 'all'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                Tous ({tenants.length})
              </button>
              <button
                onClick={() => setFilter('active')}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  filter === 'active'
                    ? 'bg-green-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                Actifs ({tenants.filter(t => t.isActive).length})
              </button>
              <button
                onClick={() => setFilter('trial')}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  filter === 'trial'
                    ? 'bg-orange-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                Essais ({tenants.filter(t => t.subscriptionStatus === 'trial').length})
              </button>
            </div>
          </div>
        </div>

        {/* Liste des tenants */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">
              Clients ({filteredTenants.length})
            </h2>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Entreprise
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    NCC
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Plan
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Statut
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Factures
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Inscrit le
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTenants.map((tenant) => {
                  const usagePercent = calculateInvoiceUsagePercent(tenant);
                  const trialDaysLeft = calculateTrialDaysLeft(tenant);

                  return (
                    <tr key={tenant.id} className="hover:bg-gray-50">
                      
                      {/* Entreprise */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center">
                            <span className="text-blue-600 font-semibold">
                              {tenant.companyName.charAt(0)}
                            </span>
                          </div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {tenant.companyName}
                            </div>
                            <div className="text-sm text-gray-500">
                              {tenant.ownerEmail}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* NCC */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-mono text-gray-900">
                          {tenant.ncc}
                        </div>
                      </td>

                      {/* Plan */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                          {SUBSCRIPTION_PLAN_LABELS[tenant.subscriptionPlan]}
                        </span>
                        <div className="text-xs text-gray-500 mt-1">
                          {SUBSCRIPTION_PLAN_PRICES[tenant.subscriptionPlan].toLocaleString()} FCFA/mois
                        </div>
                      </td>

                      {/* Statut */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          SUBSCRIPTION_STATUS_COLORS[tenant.subscriptionStatus]
                        }`}>
                          {SUBSCRIPTION_STATUS_LABELS[tenant.subscriptionStatus]}
                        </span>
                        {tenant.subscriptionStatus === 'trial' && (
                          <div className="text-xs text-gray-500 mt-1">
                            {trialDaysLeft} jours restants
                          </div>
                        )}
                        {!tenant.isActive && (
                          <div className="text-xs text-red-600 mt-1 font-medium">
                            ⚠️ Suspendu
                          </div>
                        )}
                      </td>

                      {/* Factures */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {tenant.monthlyInvoiceCount} / {tenant.monthlyInvoiceLimit}
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2 mt-1">
                          <div
                            className={`h-2 rounded-full ${
                              usagePercent >= 90 ? 'bg-red-500' :
                              usagePercent >= 70 ? 'bg-orange-500' :
                              'bg-green-500'
                            }`}
                            style={{ width: `${Math.min(usagePercent, 100)}%` }}
                          />
                        </div>
                      </td>

                      {/* Date */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {new Date(tenant.createdAt).toLocaleDateString('fr-FR')}
                      </td>

                      {/* Actions */}
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex justify-end gap-2">
                          <Link
                            to={`/admin/tenants/${tenant.id}`}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            Voir
                          </Link>
                          <button
                            onClick={() => handleToggleStatus(tenant.id, tenant.isActive)}
                            className={`${
                              tenant.isActive
                                ? 'text-red-600 hover:text-red-900'
                                : 'text-green-600 hover:text-green-900'
                            }`}
                          >
                            {tenant.isActive ? 'Suspendre' : 'Activer'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            {filteredTenants.length === 0 && (
              <div className="text-center py-12">
                <div className="text-gray-400 text-5xl mb-4">🔍</div>
                <p className="text-gray-500">Aucun client trouvé</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}