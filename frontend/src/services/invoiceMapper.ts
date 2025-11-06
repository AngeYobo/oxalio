import type { InvoiceFormModel } from "../types/invoice-ui";
import type { InvoiceRequest } from "../types/invoice";

// Lis les infos vendeur depuis env ou fallback (évite le hard-code dans le composant)
const SELLER_TAX_ID   = import.meta.env.VITE_SELLER_TAX_ID   ?? "CI00000000";
const SELLER_NAME     = import.meta.env.VITE_SELLER_NAME     ?? "Oxalio SARL";
const SELLER_ADDRESS  = import.meta.env.VITE_SELLER_ADDRESS  ?? "Abidjan, Côte d’Ivoire";
const SELLER_EMAIL    = import.meta.env.VITE_SELLER_EMAIL    ?? undefined;
const SELLER_PHONE    = import.meta.env.VITE_SELLER_PHONE    ?? undefined;

export function mapFormToRequest(form: InvoiceFormModel): InvoiceRequest {
  const subtotal = form.lines.reduce((sum, l) => sum + l.quantity * l.unitPrice, 0);
  const totalVat = form.lines.reduce((sum, l) => sum + (l.quantity * l.unitPrice * l.vatRate), 0);
  const totalDiscount = form.lines.reduce((sum, l) => sum + (l.discount ?? 0), 0);
  const totalAmount = subtotal + totalVat - totalDiscount;

  return {
    invoiceType: form.invoiceType,
    currency: form.currency,
    seller: {
      taxId: SELLER_TAX_ID,
      companyName: SELLER_NAME,
      address: SELLER_ADDRESS,
      email: SELLER_EMAIL,
      phone: SELLER_PHONE,
    },
    buyer: {
      taxId: form.buyerTaxId,
      name: form.buyerName,
      address: form.buyerAddress,
    },
    lines: form.lines.map((l) => ({
      description: l.description,
      quantity: l.quantity,
      unitPrice: l.unitPrice,
      vatRate: l.vatRate,
      vatAmount: l.quantity * l.unitPrice * l.vatRate,
      discount: l.discount ?? 0,
      productCode: undefined,
    })),
    totals: {
      subtotal,
      totalVat,
      totalAmount,
      totalDiscount,
    },
    paymentMode: form.paymentMode,
    notes: form.notes,
  };
}
