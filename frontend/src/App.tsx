import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// ========================================
// Layouts
// ========================================
import Layout from "./components/Layout";
import AdminLayout from "./components/AdminLayout";

// ========================================
// Components
// ========================================
import ProtectedRoute from "./components/ProtectedRoute";
import InvoiceList from "./components/InvoiceList";
import CreateInvoice from "./components/CreateInvoice";

// ========================================
// Pages - Authentification
// ========================================
import Login from "./pages/Login";
import Register from "./pages/Register";

// ========================================
// Pages - Multi-tenant
// ========================================
import RegisterTenant from "./pages/RegisterTenant";
import RegisterSuccess from "./pages/RegisterSuccess";

// ========================================
// Pages - Utilisateur
// ========================================
import Dashboard from "./pages/Dashboard";
import InvoiceDetail from "./pages/InvoiceDetail";
import Invoices from "./pages/Invoices";
import FneDemo from "./pages/FneDemo";

// ========================================
// Pages - Admin OXALIO
// ========================================
import AdminDashboard from "./pages/AdminDashboard";
import TenantDetail from "./pages/TenantDetail";

// ========================================
// App Component
// ========================================
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        
        {/* ==================== */}
        {/* Routes publiques     */}
        {/* ==================== */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/register-tenant" element={<RegisterTenant />} />
        <Route path="/register/success" element={<RegisterSuccess />} />

        {/* ==================== */}
        {/* Routes protégées - Utilisateur normal */}
        {/* ==================== */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          
          {/* Routes factures */}
          <Route path="invoices" element={<Invoices />} />
          <Route path="invoices/create" element={<CreateInvoice />} />
          <Route path="invoices/:id" element={<InvoiceDetail />} />
          
          {/* Autres pages */}
          <Route path="fne-demo" element={<FneDemo />} />
        </Route>

        {/* ==================== */}
        {/* Routes protégées - Admin OXALIO (SUPER_ADMIN) */}
        {/* ==================== */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requireSuperAdmin>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="tenants" element={<AdminDashboard />} />
          <Route path="tenants/:id" element={<TenantDetail />} />
        </Route>

        {/* ==================== */}
        {/* Route 404            */}
        {/* ==================== */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}

// ========================================
// Composant 404
// ========================================
function NotFound() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-gray-900 mb-4">404</h1>
        <p className="text-xl text-gray-600 mb-8">Page introuvable</p>
        
          href="/"
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition inline-block"
        <a>
          Retour à l'accueil
        </a>
      </div>
    </div>
  );
}