package com.oxalio.invoice.client;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class FneStickerClient {

    // TODO : Remplacer par un vrai appel HTTP REST vers l'API DGI quand disponible
    public String reserveStickerFromFne() {
        // Mock — génère un ID unique simulant un sticker DGI
        return "STKR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
