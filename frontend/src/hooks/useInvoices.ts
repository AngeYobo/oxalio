import { useQuery } from '@tanstack/react-query';
import { getInvoices, getInvoiceById } from '../api/invoices';

export function useInvoices() {
  return useQuery({ queryKey: ['invoices'], queryFn: getInvoices });
}

export function useInvoice(id: string) {
  return useQuery({ queryKey: ['invoice', id], queryFn: () => getInvoiceById(id) });
}
