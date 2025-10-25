package com.oxalio.invoice.service;

import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsable de la numérotation annuelle des factures
 * Exemple : INV-2025-000001
 * À terme : remplacer AtomicInteger par une séquence DB transactionnelle
 */
@Service
public class InvoiceNumberService {

    private final AtomicInteger counter = new AtomicInteger(0);
    private int currentYear = Year.now().getValue();

    public synchronized String generateInvoiceNumber() {
        int year = Year.now().getValue();
        if (year != currentYear) {
            counter.set(0); // reset chaque nouvelle année
            currentYear = year;
        }
        int next = counter.incrementAndGet();
        return String.format("INV-%d-%06d", year, next);
    }
}
