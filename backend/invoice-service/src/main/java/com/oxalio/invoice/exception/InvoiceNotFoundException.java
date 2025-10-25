package com.oxalio.invoice.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) { super(message); }
    public InvoiceNotFoundException(Long id) { super("Facture non trouvée avec l'ID : " + id); }
    public InvoiceNotFoundException(String field, String value) {
        super(String.format("Facture non trouvée avec %s : %s", field, value));
    }
}
