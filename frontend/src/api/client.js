import axios from 'axios';
export const api = axios.create({ baseURL: '/api/v1' });

console.log("🌍 Base API URL =", import.meta.env.VITE_API_URL)
