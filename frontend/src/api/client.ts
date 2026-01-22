/**
 * API Client - Axios Configuration
 * OXALIO FNE - Frontend
 * 
 * Configuration centrale pour toutes les requêtes HTTP
 * avec intercepteurs pour l'authentification et la gestion d'erreurs
 */

import axios, { AxiosError, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig} from 'axios';

// Configuration de base
const API_URL = import.meta.env.VITE_API_URL || '/api/v1';
const TIMEOUT = 30000;

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_AUTH_URL || '/api',     // -> /api/auth/...
  timeout: TIMEOUT,
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
});

export const apiV1Client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',   // -> /api/v1/invoices
  timeout: TIMEOUT,
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
});

const attachToken = (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
  const token = localStorage.getItem('token');

  if (token) {
    // robuste: s'assure d'avoir un AxiosHeaders
    config.headers = config.headers ?? new axios.AxiosHeaders();
    config.headers.set('Authorization', `Bearer ${token}`);
  }

  if (import.meta.env.VITE_DEV_MODE === 'true') {
    console.log('📤 API Request:', {
      method: config.method?.toUpperCase(),
      url: config.baseURL ? `${config.baseURL}${config.url}` : config.url,
      data: config.data,
    });
  }

  return config;
};

authClient.interceptors.request.use(attachToken);
apiV1Client.interceptors.request.use(attachToken);

// Intercepteur réponse commun (optionnel, mais utile)
const onResponse = (response: AxiosResponse) => {
  if (import.meta.env.VITE_DEV_MODE === 'true') {
    console.log('✅ API Response:', {
      status: response.status,
      url: response.config.url,
      data: response.data,
    });
  }
  return response;
};

const onError = async (error: AxiosError) => {
  if (error.response?.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('oxalio-auth-storage');
    if (window.location.pathname !== '/login') window.location.href = '/login';
  }

  if (import.meta.env.VITE_DEV_MODE === 'true') {
    console.error('💥 API Error:', {
      status: error.response?.status,
      url: error.config?.url,
      data: error.response?.data,
    });
  }

  return Promise.reject(error);
};

authClient.interceptors.response.use(onResponse, onError);
apiV1Client.interceptors.response.use(onResponse, onError)
/**
 * Instance Axios principale
 */
export const apiClient = axios.create({
  baseURL: API_URL,
  timeout: TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

/**
 * Intercepteur de requête
 * Ajoute automatiquement le token d'authentification
 */
apiClient.interceptors.request.use(
  (config: AxiosRequestConfig): any => {
    // Récupérer le token depuis localStorage
    const token = localStorage.getItem('token');
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log en mode développement
    if (import.meta.env.VITE_DEV_MODE === 'true') {
      console.log('📤 API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        data: config.data,
      });
    }
    
    return config;
  },
  (error: AxiosError) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

/**
 * Intercepteur de réponse
 * Gère les erreurs globales et le refresh du token
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log en mode développement
    if (import.meta.env.VITE_DEV_MODE === 'true') {
      console.log('✅ API Response:', {
        status: response.status,
        url: response.config.url,
        data: response.data,
      });
    }
    
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };
    
    // Gestion des erreurs HTTP
    if (error.response) {
      const status = error.response.status;
      
      switch (status) {
        case 401:
          // Non autorisé - Déconnexion automatique
          if (!originalRequest._retry) {
            console.warn('🔒 Unauthorized - Logging out');
            
            // Nettoyer le localStorage
            localStorage.removeItem('token');
            localStorage.removeItem('oxalio-auth-storage');
            
            // Rediriger vers la page de login
            if (window.location.pathname !== '/login') {
              window.location.href = '/login';
            }
          }
          break;
          
        case 403:
          // Interdit - Pas les permissions
          console.error('🚫 Forbidden - Insufficient permissions');
          break;
          
        case 404:
          // Non trouvé
          console.error('❓ Not Found:', error.config?.url);
          break;
          
        case 422:
          // Erreur de validation
          console.error('⚠️ Validation Error:', error.response.data);
          break;
          
        case 500:
          // Erreur serveur
          console.error('💥 Server Error:', error.response.data);
          break;
          
        case 503:
          // Service indisponible
          console.error('🔧 Service Unavailable');
          break;
          
        default:
          console.error(`❌ HTTP Error ${status}:`, error.response.data);
      }
    } else if (error.request) {
      // La requête a été faite mais pas de réponse
      console.error('📡 Network Error - No response received');
    } else {
      // Erreur lors de la configuration de la requête
      console.error('⚙️ Request Setup Error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

/**
 * Type pour les erreurs API formatées
 */
export interface ApiError {
  message: string;
  status?: number;
  errors?: Record<string, string[]>;
  code?: string;
}

/**
 * Utilitaire pour formater les erreurs API
 */
export const formatApiError = (error: unknown): ApiError => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<any>;
    
    if (axiosError.response) {
      return {
        message: axiosError.response.data?.message || 'Une erreur est survenue',
        status: axiosError.response.status,
        errors: axiosError.response.data?.errors,
        code: axiosError.response.data?.code || axiosError.response.data?.error,
      };
    }
    
    if (axiosError.request) {
      return {
        message: 'Impossible de contacter le serveur. Vérifiez votre connexion.',
        status: 0,
      };
    }
  }
  
  if (error instanceof Error) {
    return {
      message: error.message,
    };
  }
  
  return {
    message: 'Une erreur inconnue est survenue',
  };
};

/**
 * Utilitaire pour vérifier si une erreur est une erreur réseau
 */
export const isNetworkError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    return !error.response && !!error.request;
  }
  return false;
};

/**
 * Utilitaire pour vérifier si une erreur est une erreur d'authentification
 */
export const isAuthError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    return error.response?.status === 401;
  }
  return false;
};

/**
 * Utilitaire pour vérifier si une erreur est une erreur de validation
 */
export const isValidationError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    return error.response?.status === 422;
  }
  return false;
};

/**
 * Configuration pour les requêtes multipart/form-data
 */
export const multipartConfig: AxiosRequestConfig = {
  headers: {
    'Content-Type': 'multipart/form-data',
  },
};

/**
 * Helper pour créer des requêtes avec gestion d'erreur simplifiée
 */
export const safeApiCall = async <T>(
  apiCall: () => Promise<AxiosResponse<T>>
): Promise<{ data: T | null; error: ApiError | null }> => {
  try {
    const response = await apiCall();
    return { data: response.data, error: null };
  } catch (error) {
    return { data: null, error: formatApiError(error) };
  }
};

export const api = apiClient;

export default apiClient;