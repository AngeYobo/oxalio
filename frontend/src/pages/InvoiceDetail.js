import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useParams } from 'react-router-dom';
import { useInvoice } from '../hooks/useInvoices';
export default function InvoiceDetail() {
    const { id = '' } = useParams();
    const { data, isLoading, isError } = useInvoice(id);
    if (isLoading)
        return _jsx("p", { children: "Chargement\u2026" });
    if (isError || !data)
        return _jsx("p", { className: "text-red-600", children: "Introuvable." });
    return (_jsxs("div", { className: "bg-white border rounded-lg p-4 space-y-2", children: [_jsxs("h1", { className: "text-xl font-semibold", children: ["Facture ", data.invoiceNumber] }), _jsxs("div", { className: "text-sm text-gray-600", children: ["Client: ", data?.buyer?.name] }), _jsxs("div", { className: "text-sm text-gray-600", children: ["Montant TTC: ", data?.totals?.totalAmount, " ", data?.currency] }), _jsx("pre", { className: "text-xs bg-gray-50 p-3 rounded overflow-x-auto", children: JSON.stringify(data, null, 2) })] }));
}
