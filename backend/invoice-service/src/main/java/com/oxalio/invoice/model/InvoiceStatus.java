package com.oxalio.invoice.model;

public enum InvoiceStatus {
    RECEIVED,            // créée côté système
    SUBMITTED_TO_DGI,    // certifiée (mock ou réel)
    REJECTED,            // refusée par DGI
    CANCELLED            // annulée
}
