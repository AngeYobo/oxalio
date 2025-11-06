import axios from "axios";
import { useAuthStore } from "../store/authStore";

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8082",
  timeout: 15000,
  withCredentials: false,
});

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token; // mock/token si présent
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      useAuthStore.getState().logout();
      // On laisse le composant ProtectedRoute gérer la redirection
    }
    return Promise.reject(err);
  }
);
