import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export default function AdminQuickLogin() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();

  useEffect(() => {
    // Si déjà connecté en tant qu'admin, rediriger
    if (isAuthenticated && user?.role === 'SUPER_ADMIN') {
      navigate('/admin/dashboard');
      return;
    }

    // Sinon, mettre les données admin directement dans le store
    const adminUser = {
      id: 1,
      email: 'admin@oxalio.ci',
      name: 'Admin OXALIO',
      role: 'SUPER_ADMIN' as const,
      companyName: 'OXALIO SARL',
      companyNcc: '2505842N',
      tenantId: 1,
      createdAt: new Date().toISOString(),
    };

    const adminAuth = {
      user: adminUser,
      token: 'MOCK_SUPER_ADMIN_TOKEN',
      isAuthenticated: true,
    };

    // Sauvegarder dans localStorage
    localStorage.setItem('oxalio-auth-storage', JSON.stringify({
      state: adminAuth
    }));

    // Rediriger
    window.location.href = '/admin/dashboard';
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
        <p className="text-gray-600">Connexion admin en cours...</p>
      </div>
    </div>
  );
}