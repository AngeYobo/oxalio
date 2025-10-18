import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
export default function Layout({ children }) {
    const navigate = useNavigate();
    const { token, logout } = useAuthStore();
    return (_jsxs("div", { className: "min-h-screen bg-gray-50", children: [_jsx("header", { className: "bg-white border-b", children: _jsxs("div", { className: "max-w-6xl mx-auto px-4 py-3 flex items-center justify-between", children: [_jsx(Link, { to: "/", className: "font-semibold text-lg", children: "Oxalio" }), _jsxs("nav", { className: "flex items-center gap-4", children: [_jsx(Link, { to: "/", className: "text-sm hover:underline", children: "Dashboard" }), token ? (_jsx("button", { onClick: () => { logout(); navigate('/login'); }, className: "text-sm px-3 py-1.5 rounded border hover:bg-gray-100", children: "Se d\u00E9connecter" })) : (_jsx(Link, { to: "/login", className: "text-sm px-3 py-1.5 rounded border hover:bg-gray-100", children: "Se connecter" }))] })] }) }), _jsx("main", { className: "max-w-6xl mx-auto px-4 py-6", children: children })] }));
}
