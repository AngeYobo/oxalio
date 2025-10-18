import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Link } from 'react-router-dom';
export const NavBar = () => (_jsxs("nav", { style: { display: 'flex', gap: 16, padding: 12, borderBottom: '1px solid #eee' }, children: [_jsx(Link, { to: "/", children: "Factures" }), _jsx(Link, { to: "/login", children: "Connexion" })] }));
