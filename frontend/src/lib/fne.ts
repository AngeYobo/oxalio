import { FneClient } from './fneClient'

export const fne = new FneClient({
  baseURL: import.meta.env.VITE_FNE_BASE_URL,
  token: import.meta.env.VITE_FNE_TOKEN
})
