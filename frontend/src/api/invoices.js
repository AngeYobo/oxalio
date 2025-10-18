import axios from 'axios';
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    timeout: 10000,
});
// ⚠️ Back-end actuel expose /invoices/demo (une facture).
// On synthétise une "liste" côté front pour la démo.
export async function getInvoices() {
    const r = await api.get('/invoices/demo');
    const one = r.data;
    // génère 5 lignes "fake" à partir de la démo
    return Array.from({ length: 5 }).map((_, i) => ({
        id: `${one.invoiceNumber}-${i + 1}`,
        client: one?.buyer?.name ?? 'Client Démo',
        amount: one?.totals?.totalAmount ?? 11800,
        currency: one?.currency ?? 'XOF',
        status: one?.status ?? 'MOCK_READY',
        raw: one,
    }));
}
export async function getInvoiceById(id) {
    // Dans la vraie vie: GET /invoices/{id}
    // Ici: re-prend la démo et ajuste l'id
    const r = await api.get('/invoices/demo');
    const one = r.data;
    return {
        ...one,
        invoiceNumber: id,
    };
}
