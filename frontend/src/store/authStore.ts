/**
 * Auth Store
 * OXALIO FNE - Frontend
 * 
 * Store Zustand pour la gestion de l'authentification
 * avec persistance dans localStorage
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { apiClient } from '../api/client';

// ========================================
// Types
// ========================================

export interface User {
  id: number;
  email: string;
  name: string;
  role: 'admin' | 'user' | 'accountant' | 'SUPER_ADMIN';
  
  // Info entreprise
  companyName: string;
  companyNcc: string;
  tenantId?: number;
  
  // Optionnel
  phone?: string;
  avatar?: string;
  
  // Dates
  createdAt: string;
  lastLoginAt?: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  user: User;
  token: string;
  expiresIn: number;
}

export interface RegisterData {
  email: string;
  password: string;
  name: string;
  companyName: string;
  companyNcc: string;
  phone?: string;
}

interface AuthState {
  // État
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  register: (data: RegisterData) => Promise<void>;
  updateUser: (data: Partial<User>) => void;
  clearError: () => void;
  
  // Helpers
  hasRole: (role: User['role']) => boolean;
  isAdmin: () => boolean;
  isSuperAdmin: () => boolean; 
  isTenantAdmin: () => boolean; 
}

// ========================================
// Store
// ========================================

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // État initial
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // ========================================
      // Login
      // ========================================
      login: async (credentials: LoginCredentials) => {
        set({ isLoading: true, error: null });

        try {
          const payload = {
            email: credentials.email,
            username: credentials.email, // compat backend
            password: credentials.password,
          };

          const response = await apiClient.post<LoginResponse>(
            '/auth/login',
            payload
          );

          const { user, token } = response.data;

          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          console.log('✅ Login successful:', user.email);
        } catch (error: any) {
          const errorMessage =
            error?.response?.data?.message ??
            'Erreur de connexion. Vérifiez vos identifiants.';

          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });

          console.error('❌ Login failed:', errorMessage);
          throw new Error(errorMessage);
        }
      },


      // ========================================
      // Logout
      // ========================================
      logout: () => {
        // Supprimer le token
        localStorage.removeItem('token');
        localStorage.removeItem('oxalio-auth-storage');
        
        // Réinitialiser le store
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
        });
        
        console.log('👋 Logout successful');
        
        // Rediriger vers la page de login
        window.location.href = '/login';
      },

      // ========================================
      // Register
      // ========================================
      register: async (data: RegisterData) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await apiClient.post<LoginResponse>(
            '/auth/register',
            data
          );
          
          const { user, token } = response.data;
          
          // Sauvegarder le token
          localStorage.setItem('token', token);
          
          // Mettre à jour le store
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          
          console.log('✅ Registration successful:', user.email);
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 
                              "Erreur lors de l'inscription.";
          
          set({
            isLoading: false,
            error: errorMessage,
          });
          
          console.error('❌ Registration failed:', errorMessage);
          throw new Error(errorMessage);
        }
      },

      // ========================================
      // Update User
      // ========================================
      updateUser: (data: Partial<User>) => {
        const currentUser = get().user;
        
        if (!currentUser) {
          console.warn('⚠️ Cannot update user: not logged in');
          return;
        }
        
        const updatedUser = {
          ...currentUser,
          ...data,
        };
        
        set({ user: updatedUser });
        
        console.log('✅ User updated:', updatedUser);
      },

      // ========================================
      // Clear Error
      // ========================================
      clearError: () => {
        set({ error: null });
      },

      // ========================================
      // Helpers
      // ========================================
      
      /**
       * Vérifier si l'utilisateur a un rôle spécifique
       */
      hasRole: (role: User['role']) => {
        const { user } = get();
        return user?.role === role;
      },

      /**
       * Vérifier si l'utilisateur est admin
       */
      isAdmin: () => {
        const { user } = get();
        return user?.role === 'admin';
      },

      /**
       * Vérifier si l'utilisateur est SUPER_ADMIN (admin OXALIO)
       */
      isSuperAdmin: () => {
        const { user } = get();
        return user?.role === 'SUPER_ADMIN';
      },

      /**
       * Vérifier si l'utilisateur peut accéder aux routes admin
       */
      isTenantAdmin: () => {
        const { user } = get();
        return user?.role === 'admin' || user?.role === 'SUPER_ADMIN';
      },
    }),
    {
      name: 'oxalio-auth-storage',
      storage: createJSONStorage(() => localStorage),
      
      // Ne persister que certains champs
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// ========================================
// Hooks dérivés
// ========================================

/**
 * Hook pour vérifier si l'utilisateur est authentifié
 */
export const useIsAuthenticated = (): boolean => {
  return useAuthStore((state) => state.isAuthenticated);
};

/**
 * Hook pour récupérer l'utilisateur courant
 */
export const useCurrentUser = (): User | null => {
  return useAuthStore((state) => state.user);
};

/**
 * Hook pour vérifier si l'utilisateur est admin
 */
export const useIsAdmin = (): boolean => {
  return useAuthStore((state) => state.isAdmin());
};

/**
 * Hook pour récupérer le token
 */
export const useToken = (): string | null => {
  return useAuthStore((state) => state.token);
};

/**
 * Hook pour vérifier si l'utilisateur est SUPER_ADMIN (admin OXALIO)
 */
export const useIsSuperAdmin = (): boolean => {
  return useAuthStore((state) => state.isSuperAdmin());
};

/**
 * Hook pour vérifier si l'utilisateur peut accéder aux routes admin
 */
export const useCanAccessAdmin = (): boolean => {
  return useAuthStore((state) => state.isTenantAdmin());
};

/**
 * Hook pour récupérer le tenantId
 */
export const useTenantId = (): number | undefined => {
  return useAuthStore((state) => state.user?.tenantId);
};


// ========================================
// Utilitaires
// ========================================

/**
 * Obtenir le token depuis le store (sans hook)
 */
export const getToken = (): string | null => {
  return useAuthStore.getState().token;
};

/**
 * Obtenir l'utilisateur depuis le store (sans hook)
 */
export const getUser = (): User | null => {
  return useAuthStore.getState().user;
};

/**
 * Vérifier si l'utilisateur est authentifié (sans hook)
 */
export const isAuthenticated = (): boolean => {
  return useAuthStore.getState().isAuthenticated;
};

/**
 * Vérifier si l'utilisateur est SUPER_ADMIN (sans hook)
 */
export const isSuperAdmin = (): boolean => {
  return useAuthStore.getState().isSuperAdmin();
};

/**
 * Obtenir le tenantId (sans hook)
 */
export const getTenantId = (): number | undefined => {
  return useAuthStore.getState().user?.tenantId;
};

/**
 * Vérifier si le token est expiré
 * (nécessite que le backend envoie l'expiration)
 */
export const isTokenExpired = (): boolean => {
  const token = getToken();
  
  if (!token) {
    return true;
  }
  
  try {
    // Décoder le JWT (partie payload)
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp;
    
    if (!exp) {
      return false; // Pas d'expiration définie
    }
    
    // Vérifier si le token est expiré
    return Date.now() >= exp * 1000;
  } catch (error) {
    console.error('Error decoding token:', error);
    return true;
  }
};

/**
 * Rafraîchir le token si nécessaire
 */
export const refreshTokenIfNeeded = async (): Promise<void> => {
  if (isTokenExpired()) {
    console.log('🔄 Token expired, logging out...');
    useAuthStore.getState().logout();
  }
};



export const selectIsAuthenticated = (state: ReturnType<typeof useAuthStore.getState>) => {
  return state.isAuthenticated;
};

export default useAuthStore;