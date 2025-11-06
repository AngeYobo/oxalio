// src/components/ProtectedRoute.tsx
import { Navigate, useLocation } from "react-router-dom";
import { ReactNode } from "react";
import { useAuthStore, selectIsAuthenticated } from "../store/authStore";

type Props = { children: ReactNode };

export default function ProtectedRoute({ children }: Props) {
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <>{children}</>;
}
