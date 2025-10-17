package com.oxalio.invoice.repository;

import com.oxalio.invoice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByInvoiceNumber(String invoiceNumber);
}
