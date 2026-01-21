// src/main/java/com/oxalio/invoice/controller/SellerProfileController.java
package com.oxalio.invoice.controller;

import com.oxalio.invoice.entity.SellerProfileEntity;
import com.oxalio.invoice.service.SellerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "SellerProfiles", description = "Gestion du profil vendeur (logo, etc.)")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class SellerProfileController {

    private final SellerProfileService sellerProfileService;

    @PostMapping("/{taxId}/logo")
    @Operation(summary = "Uploader le logo du vendeur")
    public ResponseEntity<SellerProfileEntity> uploadLogo(
            @PathVariable String taxId,
            @RequestParam("file") MultipartFile file) throws Exception {

        log.info("Upload logo pour vendeur {}", taxId);
        SellerProfileEntity profile = sellerProfileService.uploadLogo(taxId, file);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{taxId}/logo")
    @Operation(summary = "Récupérer le logo du vendeur (binaire)")
    public ResponseEntity<byte[]> getLogo(@PathVariable String taxId) throws Exception {
        byte[] bytes = sellerProfileService.getLogoBytes(taxId);
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); // ou détecter selon l’extension si besoin
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{taxId}/logo")
    @Operation(summary = "Supprimer le logo du vendeur")
    public ResponseEntity<Void> deleteLogo(@PathVariable String taxId) throws Exception {
        sellerProfileService.deleteLogo(taxId);
        return ResponseEntity.noContent().build();
    }

}
