// src/pages/Login.tsx

import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useNavigate, Navigate } from 'react-router-dom';

export default function Login() {
  const [username, setU] = useState('');
  const [password, setP] = useState('');

  const token = useAuthStore((s) => s.token);
  const login = useAuthStore((s) => s.login);
  const isLoading = useAuthStore((s) => s.isLoading);
  const error = useAuthStore((s) => s.error);
  const clearError = useAuthStore((s) => s.clearError);

  const navigate = useNavigate();

  if (token) return <Navigate to="/" replace />;

  const onSubmit = async () => {
    clearError();
    try {
      await login({ email: username.trim(), password });
      navigate('/');
    } catch {
      // l’erreur est déjà mise dans le store (error)
      // on ne navigate pas
    }
  };

  return (
    <div className="max-w-sm mx-auto bg-white border rounded-lg p-6">
      <h1 className="text-lg font-semibold mb-4">Connexion</h1>

      <div className="space-y-3">
        <input
          className="w-full border rounded px-3 py-2"
          placeholder="Email / Identifiant"
          value={username}
          onChange={(e) => setU(e.target.value)}
          onFocus={() => clearError()}
        />

        <input
          className="w-full border rounded px-3 py-2"
          placeholder="Mot de passe"
          type="password"
          value={password}
          onChange={(e) => setP(e.target.value)}
          onFocus={() => clearError()}
          onKeyDown={(e) => {
            if (e.key === 'Enter') onSubmit();
          }}
        />

        {error && (
          <div className="text-sm text-red-600">
            {error}
          </div>
        )}

        <button
          onClick={onSubmit}
          disabled={isLoading || !username.trim() || !password}
          className="w-full bg-black text-white rounded px-3 py-2 disabled:opacity-60"
        >
          {isLoading ? 'Connexion…' : 'Se connecter'}
        </button>
      </div>

      <p className="text-xs text-gray-500 mt-3">
        Si le serveur renvoie une erreur (500), le problème est côté backend / API.
      </p>
    </div>
  );
}
