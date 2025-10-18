import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useNavigate, Navigate } from 'react-router-dom';
export default function Login() {
    const [username, setU] = useState('');
    const [password, setP] = useState('');
    const { token, login } = useAuthStore();
    const navigate = useNavigate();
    if (token)
        return _jsx(Navigate, { to: "/", replace: true });
    return (_jsxs("div", { className: "max-w-sm mx-auto bg-white border rounded-lg p-6", children: [_jsx("h1", { className: "text-lg font-semibold mb-4", children: "Connexion (mock)" }), _jsxs("div", { className: "space-y-3", children: [_jsx("input", { className: "w-full border rounded px-3 py-2", placeholder: "Identifiant", value: username, onChange: (e) => setU(e.target.value) }), _jsx("input", { className: "w-full border rounded px-3 py-2", placeholder: "Mot de passe", type: "password", value: password, onChange: (e) => setP(e.target.value) }), _jsx("button", { onClick: async () => { await login(username, password); navigate('/'); }, className: "w-full bg-black text-white rounded px-3 py-2", children: "Se connecter" })] }), _jsx("p", { className: "text-xs text-gray-500 mt-3", children: "Mode d\u00E9mo \u2014 toute combinaison est accept\u00E9e." })] }));
}
