package com.oxalio.invoice.service;

import com.oxalio.invoice.client.FneStickerClient;
import com.oxalio.invoice.model.Sticker;
import com.oxalio.invoice.repository.StickerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StickerManager {

    private final StickerRepository stickerRepository;
    private final FneStickerClient fneStickerClient;

    public StickerManager(StickerRepository stickerRepository, FneStickerClient fneStickerClient) {
        this.stickerRepository = stickerRepository;
        this.fneStickerClient = fneStickerClient;
    }

    @Transactional
    public String getStickerForInvoice() {
        // Étape 1 — Vérifie s'il y a un sticker disponible en local
        Sticker sticker = stickerRepository.findFirstByStatusOrderByIdAsc(Sticker.StickerStatus.AVAILABLE)
                .orElseGet(() -> fetchNewSticker());

        // Étape 2 — Réserve le sticker
        sticker.setStatus(Sticker.StickerStatus.RESERVED);
        sticker.setReservedAt(Instant.now());
        stickerRepository.save(sticker);

        return sticker.getStickerId();
    }

    private Sticker fetchNewSticker() {
        // ✅ CORRECTION : génération d'un ID temporaire
        String newStickerId = "STKR-" + System.currentTimeMillis();
        
        Sticker s = new Sticker();
        s.setStickerId(newStickerId);
        s.setStatus(Sticker.StickerStatus.AVAILABLE);
        s.setYear(String.valueOf(java.time.Year.now().getValue()));
        return stickerRepository.save(s);
    }

    @Transactional
    public void markStickerAsUsed(String stickerId) {
        Sticker sticker = stickerRepository.findAll()
                .stream()
                .filter(s -> s.getStickerId().equals(stickerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sticker not found"));

        sticker.setStatus(Sticker.StickerStatus.USED);
        sticker.setUsedAt(Instant.now());
        stickerRepository.save(sticker);
    }
}