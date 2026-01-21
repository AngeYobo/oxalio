import { Link, useLocation } from 'react-router-dom';
import { useAuthStore, useIsSuperAdmin } from '../store/authStore'; // ← MODIFIER ICI

export default function NavBar() {
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const isSuperAdmin = useIsSuperAdmin(); // ← UTILISER LE HOOK

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          
          {/* Logo et navigation */}
          <div className="flex">
            <Link to="/dashboard" className="flex items-center">
              <span className="text-2xl font-bold text-blue-600">OXALIO</span>
            </Link>

            <div className="hidden sm:ml-8 sm:flex sm:space-x-8">
              <Link
                to="/dashboard"
                className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium ${
                  isActive('/dashboard')
                    ? 'border-blue-500 text-gray-900'
                    : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                }`}
              >
                Dashboard
              </Link>
              <Link
                to="/invoices"
                className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium ${
                  isActive('/invoices')
                    ? 'border-blue-500 text-gray-900'
                    : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                }`}
              >
                Factures
              </Link>
              
              {/* Lien Admin si SUPER_ADMIN */}
              {isSuperAdmin && (
                <Link
                  to="/admin/dashboard"
                  className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-purple-600 hover:border-purple-300 hover:text-purple-700"
                >
                  🔧 Admin OXALIO
                </Link>
              )}
            </div>
          </div>

          {/* User menu */}
          <div className="flex items-center gap-4">
            <div className="text-sm">
              <div className="font-medium text-gray-900">{user?.name}</div>
              <div className="text-xs text-gray-500">{user?.companyName}</div>
            </div>
            <button
              onClick={logout}
              className="px-4 py-2 text-sm text-gray-700 hover:text-gray-900"
            >
              Se déconnecter
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}