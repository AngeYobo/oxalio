import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => {
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header Admin */}
      <header className="bg-gradient-to-r from-purple-600 to-indigo-700 shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            
            {/* Logo */}
            <div className="flex items-center">
              <Link to="/admin/dashboard" className="flex items-center">
                <div className="text-2xl font-bold text-white">
                  OXALIO <span className="text-purple-200 text-sm ml-2">Admin</span>
                </div>
              </Link>
            </div>

            {/* Navigation */}
            <nav className="hidden md:flex space-x-8">
              <Link
                to="/admin/dashboard"
                className={`px-3 py-2 rounded-md text-sm font-medium transition ${
                  isActive('/admin/dashboard')
                    ? 'bg-purple-700 text-white'
                    : 'text-purple-100 hover:bg-purple-700 hover:text-white'
                }`}
              >
                📊 Dashboard
              </Link>
              <Link
                to="/admin/tenants"
                className={`px-3 py-2 rounded-md text-sm font-medium transition ${
                  isActive('/admin/tenants')
                    ? 'bg-purple-700 text-white'
                    : 'text-purple-100 hover:bg-purple-700 hover:text-white'
                }`}
              >
                👥 Clients
              </Link>
            </nav>

            {/* User menu */}
            <div className="flex items-center gap-4">
              <div className="text-right hidden sm:block">
                <div className="text-sm font-medium text-white">
                  {user?.name || 'Admin'}
                </div>
                <div className="text-xs text-purple-200">
                  Super Administrateur
                </div>
              </div>

              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition text-sm font-medium"
              >
                Déconnexion
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Breadcrumb */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
          <div className="flex items-center text-sm text-gray-600">
            <Link to="/admin" className="hover:text-gray-900">
              Admin
            </Link>
            {location.pathname !== '/admin' && location.pathname !== '/admin/dashboard' && (
              <>
                <span className="mx-2">/</span>
                <span className="text-gray-900 font-medium">
                  {location.pathname.includes('tenants') ? 'Clients' : 'Dashboard'}
                </span>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Main content */}
      <main>
        <Outlet />
      </main>

      {/* Footer Admin */}
      <footer className="bg-white border-t border-gray-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center text-sm text-gray-600">
            <div>
              © 2026 OXALIO SARL - Plateforme SaaS Multi-tenant
            </div>
            <div className="flex gap-4">
              <Link to="/admin/settings" className="hover:text-gray-900">
                Paramètres
              </Link>
              <Link to="/admin/support" className="hover:text-gray-900">
                Support
              </Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}