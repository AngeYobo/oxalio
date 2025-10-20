// frontend/src/api/client.ts
import axios from 'axios'

// 🧭 Détection robuste de l'URL d'API
const envUrl = import.meta.env.VITE_API_BASE_URL?.trim()

// ✅ Fallback explicite pour éviter les URL relatives
const baseURL = envUrl && envUrl.length > 0
  ? envUrl
  : ''  // fallback local de dev

console.log('🌍 API Base URL =', baseURL)
console.log("🔎 VITE_API_BASE_URL (env brut) =", import.meta.env.VITE_API_BASE_URL);


export const api = axios.create({
    baseURL,
    timeout: 10000,
    auth: {
      username: 'admin',
      password: 'admin',
    },
})

// 🧰 Intercepteur d’erreurs globales — utile en prod
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('❌ API error:', error.message)
    if (error.response) {
      console.error('📡 Status:', error.response.status)
      console.error('📝 Data:', error.response.data)
    }
    return Promise.reject(error)
  }
)
