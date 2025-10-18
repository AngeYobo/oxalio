import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  // Charger les variables d'environnement depuis .env.[mode]
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react()],
    server: {
      host: true,           // 🔥 nécessaire sur Demeter
      port: 3000,
      proxy: {
        '/api': {
          target: env.VITE_API_URL,   // 👈 dynamique selon l'env
          changeOrigin: true,
          rewrite: path => path
        }
      }
    },
    build: {
      outDir: 'dist',
      sourcemap: false
    }
  }
})
