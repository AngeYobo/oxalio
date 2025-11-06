import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type User = {
  id: string;
  name?: string;
};

export interface AuthState {
  token: string | null;
  user: User | null;
  login: (username: string, password: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist<AuthState>(
    (set) => ({
      token: null,
      user: null,
      login: (username, password) => {
        const token = btoa(`${username}:${password}`); // token mock
        set({ token, user: { id: username, name: username } });
      },
      logout: () => set({ token: null, user: null }),
    }),
    { name: 'auth-store' }
  )
);

// 🚀 Ajoute et exporte ce sélecteur
export const selectIsAuthenticated = (s: AuthState) => Boolean(s.token);
