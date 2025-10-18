import { create } from 'zustand';

interface AuthState {
  token: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  login: async (username, password) => {
    // mock login: accepte tout, renvoie un token fake
    await new Promise((r) => setTimeout(r, 400));
    set({ token: 'demo-token' });
  },
  logout: () => set({ token: null }),
}));
