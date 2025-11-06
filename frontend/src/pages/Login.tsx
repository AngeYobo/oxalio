// src/pages/Longin.tsx

import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useNavigate, Navigate } from 'react-router-dom';

export default function Login() {
  const [username, setU] = useState('');
  const [password, setP] = useState('');
  const { token, login } = useAuthStore();
  const navigate = useNavigate();

  if (token) return <Navigate to="/" replace />;

  return (
    <div className="max-w-sm mx-auto bg-white border rounded-lg p-6">
      <h1 className="text-lg font-semibold mb-4">Connexion (mock)</h1>
      <div className="space-y-3">
        <input
          className="w-full border rounded px-3 py-2"
          placeholder="Identifiant"
          value={username}
          onChange={(e) => setU(e.target.value)}
        />
        <input
          className="w-full border rounded px-3 py-2"
          placeholder="Mot de passe"
          type="password"
          value={password}
          onChange={(e) => setP(e.target.value)}
        />
        <button
          onClick={async () => { await login(username, password); navigate('/'); }}
          className="w-full bg-black text-white rounded px-3 py-2"
        >
          Se connecter
        </button>
      </div>
      <p className="text-xs text-gray-500 mt-3">Mode démo — toute combinaison est acceptée.</p>
    </div>
  );
}
