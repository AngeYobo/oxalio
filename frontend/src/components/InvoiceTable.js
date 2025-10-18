import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useInvoices } from '../hooks/useInvoices';
import { Link } from 'react-router-dom';
export default function InvoiceTable() {
    const { data, isLoading, isError } = useInvoices();
    if (isLoading)
        return _jsx("p", { children: "Chargement\u2026" });
    if (isError)
        return _jsx("p", { className: "text-red-600", children: "Erreur de chargement" });
    if (!data?.length)
        return _jsx("p", { children: "Aucune facture." });
    return (_jsx("div", { className: "overflow-x-auto bg-white border rounded-lg", children: _jsxs("table", { className: "min-w-full", children: [_jsx("thead", { children: _jsxs("tr", { className: "bg-gray-100 text-left text-sm", children: [_jsx("th", { className: "p-2", children: "Num\u00E9ro" }), _jsx("th", { className: "p-2", children: "Client" }), _jsx("th", { className: "p-2", children: "Montant" }), _jsx("th", { className: "p-2", children: "Statut" }), _jsx("th", { className: "p-2" })] }) }), _jsx("tbody", { children: data.map((inv) => (_jsxs("tr", { className: "border-t", children: [_jsx("td", { className: "p-2", children: inv.id }), _jsx("td", { className: "p-2", children: inv.client }), _jsxs("td", { className: "p-2", children: [inv.amount, " ", inv.currency] }), _jsx("td", { className: "p-2", children: inv.status }), _jsx("td", { className: "p-2", children: _jsx(Link, { to: `/invoice/${encodeURIComponent(inv.id)}`, className: "text-blue-600 hover:underline text-sm", children: "D\u00E9tails" }) })] }, inv.id))) })] }) }));
}
