// src/components/ProtectedRoute.tsx
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
  requireSuperAdmin?: boolean; // ← AJOUTER
}

export default function ProtectedRoute({ 
  children, 
  requireAdmin = false,
  requireSuperAdmin = false, // ← AJOUTER
}: ProtectedRouteProps) {
  const { isAuthenticated, user, isSuperAdmin, isTenantAdmin } = useAuthStore();

  // Pas authentifié -> Redirection vers login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Route SUPER_ADMIN requise
  if (requireSuperAdmin && !isSuperAdmin()) {
    console.warn('⚠️ Access denied: SUPER_ADMIN required');
    return <Navigate to="/dashboard" replace />;
  }

  // Route admin requise (tenant admin ou super admin)
  if (requireAdmin && !isTenantAdmin()) {
    console.warn('⚠️ Access denied: Admin required');
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}