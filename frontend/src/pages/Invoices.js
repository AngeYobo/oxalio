import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { api } from '../api/client';
export const Invoices = () => {
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    useEffect(() => {
        api.get('/invoices').then(r => setRows(r.data)).finally(() => setLoading(false));
    }, []);
    if (loading)
        return _jsx("div", { style: { padding: 24 }, children: "Chargement\u2026" });
    return (_jsxs("div", { style: { padding: 24 }, children: [_jsx("h2", { children: "Factures" }), _jsxs("table", { border: 1, cellPadding: 6, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "ID" }), _jsx("th", { children: "Statut" })] }) }), _jsx("tbody", { children: rows.map((r, i) => (_jsxs("tr", { children: [_jsx("td", { children: r.id }), _jsx("td", { children: r.status })] }, i))) })] })] }));
};
