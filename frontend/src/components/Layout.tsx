import { Link, useNavigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export default function Layout() {
  const navigate = useNavigate();
  const { token, logout } = useAuthStore();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="font-semibold text-lg">Oxalio</Link>
          <nav className="flex items-center gap-4">
            <Link to="/dashboard" className="text-sm hover:underline">Dashboard</Link>
            {token ? (
              <button
                onClick={() => { logout(); navigate('/login'); }}
                className="text-sm px-3 py-1.5 rounded border hover:bg-gray-100"
              >
                Se déconnecter
              </button>
            ) : (
              <Link to="/login" className="text-sm px-3 py-1.5 rounded border hover:bg-gray-100">
                Se connecter
              </Link>
            )}
          </nav>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6">
        <Outlet />  {/* ✅ Ici s'afficheront Dashboard, Invoices, etc. */}
      </main>
    </div>
  );
}
