import type { InvoiceFormModel } from "../types/invoice-ui";
import type { InvoiceRequest, InvoiceLineDTO } from "../types/invoice-types";
import { TaxType } from "../types/invoice-types";

const SELLER_TAX_ID  = import.meta.env.VITE_SELLER_TAX_ID  ?? "CI00000000";
const SELLER_NAME    = import.meta.env.VITE_SELLER_NAME    ?? "Oxalio SARL";
const SELLER_ADDRESS = import.meta.env.VITE_SELLER_ADDRESS ?? "Abidjan, Côte d’Ivoire";
const SELLER_EMAIL   = import.meta.env.VITE_SELLER_EMAIL   ?? "";
const SELLER_PHONE   = import.meta.env.VITE_SELLER_PHONE   ?? "";

export function mapFormToRequest(form: InvoiceFormModel): InvoiceRequest {
  const subtotal = form.lines.reduce((sum, l) => sum + l.quantity * l.unitPrice, 0);

  const totalDiscount = form.lines.reduce((sum, l) => sum + (l.discount ?? 0), 0);

  const totalVat = form.lines.reduce((sum, l) => {
    const lineSubtotal = l.quantity * l.unitPrice;
    const discount = l.discount ?? 0;
    return sum + (lineSubtotal - discount) * (l.vatRate / 100);
  }, 0);

  const totalAmount = subtotal - totalDiscount + totalVat;

  const lines: InvoiceLineDTO[] = form.lines.map((l) => {
    const lineSubtotal = l.quantity * l.unitPrice;
    const discount = l.discount ?? 0;
    const vatAmount = (lineSubtotal - discount) * (l.vatRate / 100);

    return {
      description: l.description,
      quantity: l.quantity,
      unitPrice: l.unitPrice,
      taxType: (l as any).taxType ?? TaxType.TVA, // ✅ requis par InvoiceLineDTO
      vatRate: l.vatRate,
      vatAmount,
      discount,
      // ne pas forcer productCode: undefined (optionnel)
      ...(l.productCode ? { productCode: l.productCode } : {}),
    };
  });

  return {
    // champs requis InvoiceRequest
    clientName: (form as any).clientName ?? form.buyerName,
    clientNcc: (form as any).clientNcc ?? undefined,
    clientPhone: (form as any).clientPhone ?? undefined,
    clientEmail: (form as any).clientEmail ?? undefined,

    invoiceType: form.invoiceType,
    template: (form as any).template ?? "B2C",
    paymentMethod: (form as any).paymentMethod ?? form.paymentMode,
    paymentMode: form.paymentMode,
    isRne: (form as any).isRne ?? false,
    rne: (form as any).rne ?? undefined,

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
      email: (form as any).buyerEmail ?? "", // ✅ requis
      phone: (form as any).buyerPhone ?? "", // ✅ requis
    },

    lines,

    totals: {
      subtotal,
      totalVat,
      totalDiscount,
      totalAmount,
    },

    notes: form.notes,
  };
}
