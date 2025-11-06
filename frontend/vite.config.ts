// vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 3000,
    strictPort: true, // force l’usage du 3000 (sinon Vite bascule en 3001)
    allowedHosts: [
      '3000-sidereal-election-ozhudl.us1.demeter.run'
    ],
    hmr: {
      protocol: 'wss', // Demeter est en HTTPS -> websocket sécurisé
      host: '3000-sidereal-election-ozhudl.us1.demeter.run',
      clientPort: 443
    }
  }
})
