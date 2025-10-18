import { create } from 'zustand';
export const useAuthStore = create((set) => ({
    token: null,
    login: async (username, password) => {
        // mock login: accepte tout, renvoie un token fake
        await new Promise((r) => setTimeout(r, 400));
        set({ token: 'demo-token' });
    },
    logout: () => set({ token: null }),
}));
